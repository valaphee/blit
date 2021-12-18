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
import org.apache.sshd.client.SshClient
import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.client.impl.DefaultSftpClientFactory
import org.apache.sshd.sftp.common.SftpException

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("sftp")
class SftpSource(
    name: String,
    @get:JsonProperty("host") val host: String,
    @get:JsonProperty("port") val port: Int,
    @get:JsonProperty("username") val username: String,
    @get:JsonProperty("password") val password: String
) : AbstractSource<SftpEntry>(name) {
    @get:JsonIgnore internal val sftpClient: SftpClient by lazy {
        val sshSession = ssh.connect(username, host, port).verify(30000).session
        sshSession.addPasswordIdentity(password)
        sshSession.auth().verify(30000)
        DefaultSftpClientFactory.INSTANCE.createSftpClient(sshSession)
    }

    override val home get() = "." // TODO: pwd

    override suspend fun isValid(path: String) = try {
        sftpClient.stat(path).isDirectory
    } catch (_: SftpException) {
        false
    }

    override suspend fun get(path: String) = SftpEntry(this, path, sftpClient.stat(path))

    companion object {
        private val ssh = SshClient.setUpDefaultClient().apply { start() }
    }
}
