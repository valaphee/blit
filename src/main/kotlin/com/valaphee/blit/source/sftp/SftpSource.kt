/*
 * Copyright (c) 2021, Valaphee.
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

package com.valaphee.blit.source.sftp

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.source.AbstractSource
import com.valaphee.blit.source.NotFoundException
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKeyPairResourceParser
import org.apache.sshd.core.CoreModuleProperties
import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.client.SftpClientFactory
import org.apache.sshd.sftp.common.SftpConstants
import org.apache.sshd.sftp.common.SftpException
import java.nio.file.Paths
import java.time.Duration

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("sftp")
class SftpSource(
    name: String  = "",
    @get:JsonProperty("host") val host: String  = "",
    @get:JsonProperty("port") val port: Int = 22,
    @get:JsonProperty("username") val username: String  = "",
    @get:JsonProperty("password") val password: String = "",
    @get:JsonProperty("private_key") val privateKey: String = ""
) : AbstractSource<SftpEntry>(name) {
    @get:JsonIgnore private val sshSession: ClientSession by lazy {
        val sshSession = sshClient.connect(username, host, port).verify().session
        if (password.isNotEmpty()) sshSession.addPasswordIdentity(password)
        if (privateKey.isNotEmpty()) OpenSSHKeyPairResourceParser.INSTANCE.loadKeyPairs(null, Paths.get(privateKey), { _, _, _ -> TODO() }).firstOrNull()?.let { sshSession.addPublicKeyIdentity(it) }
        sshSession.auth().verify()
        sshSession
    }
    @get:JsonIgnore internal val sftpClient: SftpClient by lazy { SftpClientFactory.instance().createSftpClient(sshSession) }

    override val home: String get() = sshSession.executeRemoteCommand("pwd").lines().first()

    override suspend fun get(path: String) = try {
        SftpEntry(this, path, sftpClient.stat(path))
    } catch (ex: SftpException) {
        when (ex.status) {
            SftpConstants.SSH_FX_NO_SUCH_FILE -> throw NotFoundException(path)
            else -> throw ex
        }
    }

    companion object {
        internal val sshClient = SshClient.setUpDefaultClient().apply {
            CoreModuleProperties.HEARTBEAT_INTERVAL.set(this, Duration.ofSeconds(2))
            start()
        }
    }
}
