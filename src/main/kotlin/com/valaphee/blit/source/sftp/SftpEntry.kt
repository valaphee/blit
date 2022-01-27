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

package com.valaphee.blit.source.sftp

import com.valaphee.blit.source.AbstractEntry
import com.valaphee.blit.source.NotFoundException
import com.valaphee.blit.util.transferToWithProgress
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.sync.withPermit
import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.common.SftpConstants
import org.apache.sshd.sftp.common.SftpException
import java.io.InputStream
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
class SftpEntry(
    private val source: SftpSource,
    override val path: String,
    private var attributes: SftpClient.Attributes
) : AbstractEntry<SftpEntry>() {
    override val size get() = attributes.size
    override val modifyTime get() = attributes.modifyTime.toMillis()
    override val directory get() = attributes.isDirectory

    override suspend fun list() = if (directory) try {
        source.semaphore.withPermit {
            source.pool.useInstance {
                it.readDir(path).mapNotNull {
                    val name = it.filename
                    if (name != "." && name != "..") SftpEntry(source, "${if (path == "/") "" else path}/$name", it.attributes) else null
                }
            }
        }
    } catch (_: RuntimeException) {
        emptyList()
    } else emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        try {
            source.semaphore.withPermit { source.pool.useInstance { it.read(path).use { it.transferToWithProgress(stream, size) } } }
        } catch (ex: SftpException) {
            when (ex.status) {
                SftpConstants.SSH_FX_NO_SUCH_FILE -> throw NotFoundException(path)
                else -> throw ex
            }
        }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) {
        check(directory)

        source.semaphore.withPermit { source.pool.useInstance { it.write("$path/$name").use { stream.transferToWithProgress(it, length) } } }
    }

    override suspend fun rename(path: String) {
        try {
            source.semaphore.withPermit { source.pool.useInstance { it.rename(this.path, path) } }
        } catch (ex: SftpException) {
            when (ex.status) {
                SftpConstants.SSH_FX_NO_SUCH_FILE -> throw NotFoundException(this.path)
                else -> throw ex
            }
        }
    }

    override suspend fun delete() {
        try {
            source.semaphore.withPermit { source.pool.useInstance { it.remove(path) } }
        } catch (ex: SftpException) {
            when (ex.status) {
                SftpConstants.SSH_FX_NO_SUCH_FILE -> throw NotFoundException(path)
                else -> throw ex
            }
        }
    }
}
