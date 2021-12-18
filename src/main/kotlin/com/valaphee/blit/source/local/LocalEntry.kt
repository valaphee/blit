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

package com.valaphee.blit.source.local

import com.valaphee.blit.source.AbstractEntry
import com.valaphee.blit.source.transferToWithProgress
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
class LocalEntry(
    private val path: File
) : AbstractEntry<LocalEntry>() {
    override val name: String get() = path.name
    override val size get() = path.length()
    override val modifyTime get() = path.lastModified()
    override val directory get() = path.isDirectory

    override suspend fun list() = path.listFiles()?.map { LocalEntry(it) } ?: emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        FileInputStream(path).use { it.transferToWithProgress(stream, size) }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) {
        FileOutputStream("$path/$name").use { stream.transferToWithProgress(it, size) }
    }

    override suspend fun delete() {
        path.delete()
    }

    override fun toString() = path.toString()
}
