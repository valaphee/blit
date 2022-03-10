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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.TypeLiteral
import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.util.Types
import io.github.classgraph.ClassGraph
import java.io.File

/**
 * Responsible for finding, loading and making [Data] types available for dependency injection.
 *
 * @property path the path to search for data.
 *
 * @author Kevin Ludwig
 */
class DataModule(
    private val path: File
) : AbstractModule() {
    @Inject lateinit var objectMapper: ObjectMapper

    override fun configure() {
        if (!::objectMapper.isInitialized) objectMapper = jacksonObjectMapper().apply { registerModule(AfterburnerModule()) }.also { bind(ObjectMapper::class.java).toInstance(it) }

        ClassGraph().acceptPaths("data").scan().use {
            val (keyedData, data) = (it.allResources.map { it.url } + path.walk().filter { it.isFile }.map { it.toURI().toURL() })
                .mapNotNull {
                    when (it.file.substring(it.file.lastIndexOf('.') + 1)) {
                        "json" -> try {
                            objectMapper.readValue<Data>(it)
                        } catch (_: Exception) {
                            null
                        }
                        else -> null
                    }
                }
                .partition { it is KeyedData }
            keyedData.filterIsInstance<KeyedData>()
                .groupBy { it::class }
                .forEach { (_, value) ->
                    @Suppress("UNCHECKED_CAST")
                    (bind(TypeLiteral.get(Types.mapOf(String::class.java, value.first()::class.java))) as AnnotatedBindingBuilder<Any>).toInstance(value.associateBy { it.key })
                }
            data.forEach {
                @Suppress("UNCHECKED_CAST")
                (bind(it::class.java) as AnnotatedBindingBuilder<Any>).toInstance(it)
            }
        }
    }
}
