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

package com.valaphee.blit.source.ftp

import com.valaphee.blit.source.AbstractEntry
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.sync.withPermit
import org.apache.commons.net.ftp.FTPFile
import java.io.InputStream
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
class FtpEntry(
    private val source: FtpSource,
    override val path: String,
    private val ftpFile: FTPFile
) : AbstractEntry<FtpEntry>() {
    override val size get() = ftpFile.size
    override val modifyTime get() = ftpFile.timestamp.timeInMillis
    override val directory get() = ftpFile.isDirectory

    override suspend fun makeDirectory(name: String) = TODO()

    override suspend fun list() = if (directory) source.semaphore.withPermit { source.pool.useInstance { it.listFiles(path).mapNotNull { FtpEntry(source, "${if (path == "/") "" else path}/$name", it) } } } else emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        check(!directory)

        TODO()
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) {
        check(directory)

        TODO()
    }

    override suspend fun rename(path: String) = TODO()

    override suspend fun delete() = TODO()
}
