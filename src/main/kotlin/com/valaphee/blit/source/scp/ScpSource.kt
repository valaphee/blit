/*
 * Copyright (c) 2021-2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valaphee.blit.source.scp

import com.valaphee.blit.source.NotFoundException
import com.valaphee.blit.source.Source
import com.valaphee.blit.source.sftp.SftpSource
import io.ktor.utils.io.pool.DefaultPool
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKeyPairResourceParser
import org.apache.sshd.scp.client.ScpClient
import org.apache.sshd.scp.client.ScpClientCreator
import java.nio.file.Paths

/**
 * @author Kevin Ludwig
 */
class ScpSource(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val privateKey: String,
    private val connectionPoolSize: Int
) : Source<ScpEntry> {
    internal val semaphore = Semaphore(connectionPoolSize)
    internal val pool = object : DefaultPool<ScpClient>(connectionPoolSize) {
        override fun produceInstance(): ScpClient {
            val sshSession = SftpSource.sshClient.connect(username, host, port).verify().session
            if (password.isNotEmpty()) sshSession.addPasswordIdentity(password)
            if (privateKey.isNotEmpty()) OpenSSHKeyPairResourceParser.INSTANCE.loadKeyPairs(null, Paths.get(privateKey), { _, _, _ -> TODO() }).firstOrNull()?.let { sshSession.addPublicKeyIdentity(it) }
            sshSession.auth().verify()
            return ScpClientCreator.instance().createScpClient(sshSession)
        }

        override fun clearInstance(instance: ScpClient) = if (instance.session.isOpen) instance else {
            disposeInstance(instance)
            produceInstance()
        }

        override fun disposeInstance(instance: ScpClient) {
            instance.session.close()
        }
    }

    override val home get() = runBlocking { semaphore.withPermit { pool.useInstance { it.session.executeRemoteCommand("pwd").lines().first() } } }

    override suspend fun get(path: String) = parseLsEntry(semaphore.withPermit { pool.useInstance { it.session.executeRemoteCommand("""stat --format "%A 0 %U %G %s %y %n" "$path"""").lines().first() } })?.second?.let { ScpEntry(this, path, it) } ?: throw NotFoundException(path)

    override fun close() {
        pool.close()
    }
}
