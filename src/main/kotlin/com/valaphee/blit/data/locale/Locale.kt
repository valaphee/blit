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

package com.valaphee.blit.data.locale

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.blit.data.DataType
import com.valaphee.blit.data.KeyedData
import java.text.DateFormat
import java.text.MessageFormat
import java.util.regex.Pattern

/**
 * Locale data
 *
 * @author Kevin Ludwig
 */
@DataType("locale")
class Locale(
    @get:JsonProperty("key") override val key: String,
    @get:JsonProperty("entries") val entries: Map<String, Any>
) : KeyedData() {
    private val entriesFlat = entries.flatMap { flatten(it.key, it.value) }.toMap()
    private val entryFormats = mutableMapOf<String, MessageFormat>()

    /**
     * Shortcut for getting the Locale-specific date format
     */
    val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, toJavaLocale())

    /**
     * Retrieve locale strings with formatting.
     *
     * @param key Key of the locale string
     * @param arguments Arguments which are put into the specific placeholder (e.g. {0},...)
     * @return Formatted locale string
     */
    operator fun get(key: String, vararg arguments: Any?) = entriesFlat[key]?.let {
        (entryFormats[key] ?: (try {
            MessageFormat(it)
        } catch (_: IllegalArgumentException) {
            MessageFormat(pattern.matcher(it).replaceAll("\\[$1\\]"))
        }).also { entryFormats[key] = it }).format(arguments)
    } ?: key

    /**
     * Java Locale
     */
    private fun toJavaLocale(): java.util.Locale = java.util.Locale.forLanguageTag(key.replace('_', '-'))

    companion object {
        private val pattern = Pattern.compile("\\{(\\D*?)}")

        private fun flatten(key: String, value: Any): Iterable<Pair<String, String>> = if (value !is Map<*, *>) listOf(key to value.toString()) else value.flatMap { flatten("$key.${(it.key as String)}", it.value!!) }
    }
}
