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

package com.valaphee.blit.source.k8scp

import com.valaphee.blit.source.AbstractEntry
import org.apache.sshd.sftp.client.SftpClient
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
class K8scpEntry(
    private val k8scpSource: K8scpSource,
    private val path: String,
    private val attributes: SftpClient.Attributes
) : AbstractEntry<K8scpEntry>() {
    override val name = path.removeSuffix("/").split('/').last()
    override val size get() = attributes.size
    override val modifyTime get() = attributes.modifyTime.toMillis()
    override val directory get() = attributes.isDirectory

    override suspend fun list() = if (directory) {
        val process = K8scpSource.exec.exec(k8scpSource.namespace, k8scpSource.pod, arrayOf("ls", "-l", "--full-time", path), false)
        val list = BufferedReader(InputStreamReader(process.inputStream)).use { it.readLines().mapNotNull { parseLsEntry(it)?.let { K8scpEntry(k8scpSource, "$path/${it.first}", it.second) } } }
        process.waitFor()
        list
    } else emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        K8scpSource.copy.copyFileFromPod(k8scpSource.namespace, k8scpSource.pod, toString()).use { it.transferTo(stream) }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) = TODO()

    override fun toString() = path
}
