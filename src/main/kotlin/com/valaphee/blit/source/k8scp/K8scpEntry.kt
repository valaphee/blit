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
import com.valaphee.blit.source.NotFoundException
import com.valaphee.blit.source.scp.parseLsEntry
import com.valaphee.blit.source.transferToWithProgress
import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.common.SftpConstants
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
class K8scpEntry(
    private val source: K8scpSource,
    private val path: String,
    private val attributes: SftpClient.Attributes
) : AbstractEntry<K8scpEntry>() {
    override val name = path.removeSuffix("/").split('/').last()
    override val size get() = attributes.size
    override val modifyTime get() = attributes.modifyTime.toMillis()
    override val directory get() = attributes.isDirectory

    override suspend fun list() = if (directory) {
        val (namespace, pod, path) = source.getNamespacePodAndPath(path)
        if (namespace != null) {
            if (pod != null) {
                val process = K8scpSource.copy.exec(namespace, pod, arrayOf("ls", "-l", "--full-time", path!!), false)
                val list = BufferedReader(InputStreamReader(process.inputStream)).use { it.readLines().mapNotNull { parseLsEntry(it)?.let { K8scpEntry(source, "${if (this.path == "/") "" else this.path}/${it.first}", it.second) } } }
                process.waitFor()
                list
            } else K8scpSource.coreV1Api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null).items.map { K8scpEntry(source, "${if (this.path == "/") "" else this.path}/${it.metadata!!.name!!}", namespaceOrPodAttributes) }
        } else emptyList()
    } else emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        val (namespace, pod, path) = source.getNamespacePodAndPath(path)
        K8scpSource.copy.copyFileFromPod(namespace, pod, path!!).use { it.transferToWithProgress(stream, size) }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) = TODO()

    override suspend fun rename(name: String) {
        val (namespace, pod, path) = source.getNamespacePodAndPath(path)
        if (K8scpSource.copy.exec(namespace, pod, arrayOf("mv", path!!, "${path.substringBeforeLast('/', "")}/$name"), false).waitFor() != 0) {
            throw NotFoundException(name)
        }
    }

    override suspend fun delete() {
        val (namespace, pod, path) = source.getNamespacePodAndPath(path)
        if (K8scpSource.copy.exec(namespace, pod, arrayOf("rm", "-rf", path!!), false).waitFor() != 0) {
            throw NotFoundException(name)
        }
    }

    override fun toString() = path

    companion object {
        internal val namespaceOrPodAttributes = SftpClient.Attributes().apply {
            permissions = SftpConstants.S_IFDIR
            modifyTime(0)
        }
    }
}
