/*
 * MIT License
 *
 * Copyright (c) 2021, Valaphee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.valaphee.blit.sftp

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.AbstractSource
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
    @get:JsonProperty("password") val password: String,
) : AbstractSource<SftpEntry>(name) {
    @get:JsonIgnore internal val sftpClient: SftpClient by lazy {
        val sshSession = ssh.connect(username, host, port).verify(30000).session
        sshSession.addPasswordIdentity(password)
        sshSession.auth().verify(30000)
        DefaultSftpClientFactory.INSTANCE.createSftpClient(sshSession)
    }

    override val home get() = "." // TODO

    override fun isValid(path: String) = try {
        sftpClient.stat(path).isDirectory
    } catch (_: SftpException) {
        false
    }

    override fun get(path: String) = SftpEntry(this, path, "")

    companion object {
        private val ssh = SshClient.setUpDefaultClient().apply { start() }
    }
}
