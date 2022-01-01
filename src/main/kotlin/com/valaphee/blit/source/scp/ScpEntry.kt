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

package com.valaphee.blit.source.scp

import com.valaphee.blit.progress
import com.valaphee.blit.source.AbstractEntry
import com.valaphee.blit.source.TransferInputStream
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.sync.withPermit
import org.apache.sshd.scp.common.helpers.ScpTimestampCommandDetails
import org.apache.sshd.sftp.client.SftpClient
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.attribute.PosixFilePermission
import kotlin.coroutines.coroutineContext

/**
 * @author Kevin Ludwig
 */
class ScpEntry(
    private val source: ScpSource,
    private val path: String,
    private var attributes: SftpClient.Attributes
) : AbstractEntry<ScpEntry>() {
    override val name = path.removeSuffix("/").split('/').last()
    override val size get() = attributes.size
    override val modifyTime get() = attributes.modifyTime.toMillis()
    override val directory get() = attributes.isDirectory

    override suspend fun list() = if (directory) source.semaphore.withPermit { source.pool.useInstance { it.session.executeRemoteCommand("""ls -al --full-time "$path"""").lines().mapNotNull { parseLsEntry(it)?.let { (name, attributes) -> if (name != "." && name != "..") ScpEntry(source, "${if (this.path == "/") "" else this.path}/$name", attributes) else null } } } } else emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        source.semaphore.withPermit { source.pool.useInstance { it.download(path, stream) } }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) {
        val coroutineContext = coroutineContext
        val time = System.currentTimeMillis()
        source.semaphore.withPermit { source.pool.useInstance { it.upload(TransferInputStream(stream) { coroutineContext.progress = it / length.toDouble()}, "$path/$name", length, permissions, ScpTimestampCommandDetails(time, time)) } }
    }

    override suspend fun rename(name: String) {
        source.semaphore.withPermit { source.pool.useInstance { it.session.executeRemoteCommand("""mv "$path" "${path.substringBeforeLast('/', "")}/$name"""") } }
    }

    override suspend fun delete() {
        source.semaphore.withPermit { source.pool.useInstance { it.session.executeRemoteCommand("""rm -rf "$path"""") } }
    }

    override fun toString() = path

    companion object {
        private val permissions = listOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE)
    }
}
