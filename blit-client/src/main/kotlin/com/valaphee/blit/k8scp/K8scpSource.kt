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

package com.valaphee.blit.k8scp

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.blit.Source
import io.kubernetes.client.Copy
import io.kubernetes.client.Exec
import io.kubernetes.client.openapi.Configuration
import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.common.SftpConstants

/**
 * @author Kevin Ludwig
 */
class K8scpSource(
    name: String,
    @get:JsonProperty("namespace") val namespace: String,
    @get:JsonProperty("pod") val pod: String
) : Source<K8scpEntry>(name) {
    internal val exec = Exec()
    internal val copy = Copy()

    override val home get() = "." // TODO

    override fun isValid(path: String) = true // TODO

    override fun get(path: String) = K8scpEntry(this, path, "", SftpClient.Attributes().apply { permissions = SftpConstants.S_IFDIR }) // TODO

    companion object {
        init {
            Configuration.setDefaultApiClient(io.kubernetes.client.util.Config.defaultClient())
        }
    }
}
