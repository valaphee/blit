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

package com.valaphee.blit.data

/**
 * A [KeyedData] is a special kind of [Data] which will be available as a Map for dependency injection
 * and doesn't have to be a singleton.
 *
 * Remind that Kotlin uses automatic wildcard types for JVM for this reason use Map<String, @JvmSuppressWildcards (subtype of KeyedData)>.
 *
 * @author Kevin Ludwig
 */
abstract class KeyedData : Data {
    abstract val key: String

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyedData

        if (key != other.key) return false

        return true
    }

    override fun hashCode() = key.hashCode()

    override fun toString() = key
}
