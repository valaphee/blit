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

    override suspend fun delete() = TODO()

    override fun toString() = path
}
