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

import javafx.scene.control.TreeItem
import java.io.InputStream
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
interface Entry<T : Entry<T>> {
    val item: TreeItem<Entry<T>>

    val self: Entry<T>

    val name: String
    val size: Long
    val modifyTime: Long
    val directory: Boolean

    suspend fun list(): List<T>

    suspend fun transferTo(stream: OutputStream)

    suspend fun transferFrom(name: String, stream: InputStream, length: Long)

    suspend fun rename(name: String)

    suspend fun delete()
}
