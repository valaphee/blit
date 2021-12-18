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

import com.valaphee.blit.source.AbstractEntry
import org.apache.sshd.sftp.client.SftpClient
import java.io.InputStream
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
class SftpEntry(
    private val sftpSource: SftpSource,
    private val path: String,
    private var attributes: SftpClient.Attributes
) : AbstractEntry<SftpEntry>() {
    override val name = path.removeSuffix("/").split('/').last()
    override val size get() = attributes.size
    override val modifyTime get() = attributes.modifyTime.toMillis()
    override val directory get() = attributes.isDirectory

    override suspend fun list() = if (directory) try {
        sftpSource.sftpClient.readDir(path).mapNotNull {
            val name = it.filename
            if (name != "." && name != "..") SftpEntry(sftpSource, "$path/$name", it.attributes) else null
        }
    } catch (_: RuntimeException) {
        emptyList()
    } else emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        sftpSource.sftpClient.read(toString()).use { it.transferTo(stream) }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) = TODO()

    override suspend fun delete() = TODO()

    override fun toString() = path
}
