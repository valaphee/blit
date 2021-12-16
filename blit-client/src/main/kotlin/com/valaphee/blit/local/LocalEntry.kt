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

package com.valaphee.blit.local

import com.valaphee.blit.AbstractEntry
import java.io.File
import java.io.FileInputStream
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

    override val children get() = path.listFiles()?.map { LocalEntry(it) } ?: emptyList()

    override fun transferTo(stream: OutputStream) {
        FileInputStream(path).use { it.transferTo(stream) }
    }

    override fun toString() = path.toString()
}
