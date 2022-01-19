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

package com.valaphee.blit.util

import java.io.FilterOutputStream
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
class TransferOutputStream(
    private val stream: OutputStream,
    private val onWrite: (Long) -> Unit
) : FilterOutputStream(stream) {
    private var writeSum = 0L

    override fun write(b: Int) {
        stream.write(b)
        writeSum++
        onWrite(writeSum)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        stream.write(b, off, len)
        writeSum += len
        onWrite(writeSum)
    }
}
