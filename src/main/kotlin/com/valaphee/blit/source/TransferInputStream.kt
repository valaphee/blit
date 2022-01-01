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

package com.valaphee.blit.source

import java.io.FilterInputStream
import java.io.InputStream

/**
 * @author Kevin Ludwig
 */
class TransferInputStream(
    private val stream: InputStream,
    private val onRead: (Long) -> Unit
) : FilterInputStream(stream) {
    private var readSum = 0L
    private var mark = -1L

    override fun read(): Int {
        val result = stream.read()
        if (result != -1) {
            readSum++
            onRead(readSum)
        }
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result = stream.read(b, off, len)
        if (result != -1) {
            readSum += result
            onRead(readSum)
        }
        return result
    }

    override fun skip(n: Long): Long {
        val result = stream.skip(n)
        readSum += result
        return result
    }

    override fun mark(readlimit: Int) {
        stream.mark(readlimit)
        mark = readSum
    }

    override fun reset() {
        require(stream.markSupported())
        require(mark != -1L)

        stream.reset()

        readSum = mark
        onRead(readSum)
    }
}
