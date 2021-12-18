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

import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.source.AbstractSource
import java.io.File

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("local")
class LocalSource(
    name: String
) : AbstractSource<LocalEntry>(name) {
    override val home: String get() = File(System.getProperty("user.home")).absolutePath

    override suspend fun isValid(path: String) = File(path).isDirectory

    override suspend fun get(path: String) = LocalEntry(File(path))
}
