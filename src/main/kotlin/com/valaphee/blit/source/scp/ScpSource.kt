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

package com.valaphee.blit.source.scp

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.source.AbstractSource
import com.valaphee.blit.source.NotFoundException
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKeyPairResourceParser
import org.apache.sshd.scp.client.ScpClient
import org.apache.sshd.scp.client.ScpClientCreator
import java.nio.file.Paths

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("scp")
class ScpSource(
    name: String  = "",
    @get:JsonProperty("host") val host: String  = "",
    @get:JsonProperty("port") val port: Int = 22,
    @get:JsonProperty("username") val username: String  = "",
    @get:JsonProperty("password") val password: String = "",
    @get:JsonProperty("private_key") val privateKey: String = ""
) : AbstractSource<ScpEntry>(name) {
    @get:JsonIgnore internal val sshSession: ClientSession by lazy {
        val sshSession = sshClient.connect(username, host, port).verify().session
        if (password.isNotEmpty()) sshSession.addPasswordIdentity(password)
        if (privateKey.isNotEmpty()) OpenSSHKeyPairResourceParser.INSTANCE.loadKeyPairs(null, Paths.get(privateKey), { _, _, _ -> TODO() }).firstOrNull()?.let { sshSession.addPublicKeyIdentity(it) }
        sshSession.auth().verify()
        sshSession
    }
    @get:JsonIgnore internal val scpClient: ScpClient by lazy { ScpClientCreator.instance().createScpClient(sshSession) }

    override val home: String get() = sshSession.executeRemoteCommand("pwd").lines().first()

    override suspend fun get(path: String) = parseLsEntry(sshSession.executeRemoteCommand("""stat --format "%A 0 %U %G %s %y %n" "$path"""").lines().first())?.second?.let { ScpEntry(this, path, it) } ?: throw NotFoundException(path)

    companion object {
        private val sshClient = SshClient.setUpDefaultClient().apply { start() }
    }
}
