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

import com.valaphee.blit.progress
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.pool.ByteArrayPool
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.coroutineContext

suspend fun InputStream.transferToWithProgress(stream: OutputStream, length: Long) {
    val buffer = ByteArrayPool.borrow()
    try {
        var readSum = 0L
        while (true) {
            val read = read(buffer, 0, buffer.size)
            if (read == -1) break
            if (read > 0) {
                stream.write(buffer, 0, read)
                readSum += read
            }
            coroutineContext.progress = readSum / length.toDouble()
        }
    } finally {
        ByteArrayPool.recycle(buffer)
    }
}

suspend fun ByteReadChannel.transferToWithProgress(stream: OutputStream, length: Long) {
    val buffer = ByteArrayPool.borrow()
    try {
        var readSum = 0L
        while (true) {
            val read = readAvailable(buffer, 0, buffer.size)
            if (read == -1) break
            if (read > 0) {
                stream.write(buffer, 0, read)
                readSum += read
            }
            coroutineContext.progress = readSum / length.toDouble()
        }
    } finally {
        ByteArrayPool.recycle(buffer)
    }
}
