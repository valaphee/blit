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

package com.valaphee.blit.locale

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.data.KeyedData
import java.text.DateFormat
import java.text.MessageFormat
import java.util.regex.Pattern

/**
 * A [Locale] is a kind of [KeyedData] which is used for I18n to store texts which are translated into different
 * languages, which the user can choose from. It is used by Jackson for persistence and will be injected
 * as a map.
 *
 * Remind that Kotlin uses automatic wildcard types for JVM for this reason use Map<String, @JvmSuppressWildcards Locale>.
 *
 * @property key the key used by jackson serialization.
 * @property entries the locale-specific entries (tree-structure).
 *
 * @author Kevin Ludwig
 */
@JsonTypeName("locale")
class Locale(
    @get:JsonProperty("key") override val key: String,
    @get:JsonProperty("entries") val entries: Map<String, Any>
) : KeyedData() {
    // To remove re-occurring parts of the key, entries are saved in a tree-structure, for retrieving a specific entry, they have to be flattened.
    private val entriesFlat = entries.flatMap { flatten(it.key, it.value) }.toMap()
    private val entryFormats = mutableMapOf<String, MessageFormat>()

    /**
     * Shorthand for accessing the locale-specific date time format
     */
    val dateTimeFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, java.util.Locale.forLanguageTag(key.replace('_', '-')))

    /**
     * Get formatted texts
     *
     * @param key the key of the text, the tree hierarchy will be converted in a flat map and uses . as delimiter.
     * @param arguments the arguments which are put into their respective placeholder (e.g. {0},...)
     * @return the formatted text
     */
    operator fun get(key: String, vararg arguments: Any?) = entriesFlat[key]?.let {
        (entryFormats[key] ?: (try {
            MessageFormat(it)
        } catch (_: IllegalArgumentException) {
            MessageFormat(pattern.matcher(it).replaceAll("\\[$1\\]"))
        }).also { entryFormats[key] = it }).format(arguments)
    } ?: key

    companion object {
        private val pattern = Pattern.compile("\\{(\\D*?)}")

        private fun flatten(key: String, value: Any): Iterable<Pair<String, String>> = if (value !is Map<*, *>) listOf(key to value.toString()) else value.flatMap { flatten("$key.${(it.key as String)}", it.value!!) }
    }
}
