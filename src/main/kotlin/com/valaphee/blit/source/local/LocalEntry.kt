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

package com.valaphee.blit.source.local

import com.valaphee.blit.source.AbstractEntry
import com.valaphee.blit.source.NotFoundError
import com.valaphee.blit.util.transferToWithProgress
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
class LocalEntry(
    private val file: File
) : AbstractEntry<LocalEntry>() {
    override val path: String get() = file.path.replace('\\', '/')
    override val name: String get() = file.name
    override val size = file.length()
    override val modifyTime = file.lastModified()
    override val directory get() = file.isDirectory

    override suspend fun makeDirectory(name: String) {
        check(directory)

        File(file, name).mkdir()
    }

    override suspend fun list() = file.listFiles()?.map { LocalEntry(it) } ?: emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        check(!directory)

        try {
            FileInputStream(file).use { it.transferToWithProgress(stream, size) }
        } catch (ex: FileNotFoundException) {
            throw NotFoundError(path)
        }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) {
        check(directory)

        FileOutputStream(File(file, name)).use { stream.transferToWithProgress(it, size) }
    }

    override suspend fun rename(path: String) {
        file.renameTo(File(path))
    }

    override suspend fun delete() {
        file.delete()
    }
}
