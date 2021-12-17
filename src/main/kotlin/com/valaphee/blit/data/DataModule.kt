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

package com.valaphee.blit.data

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
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
 * @author Kevin Ludwig
 */
class DataModule(
    private val path: File = File("data")
) : AbstractModule() {
    @Inject lateinit var objectMapper: ObjectMapper

    override fun configure() {
        if (!::objectMapper.isInitialized) objectMapper = jacksonObjectMapper().apply {
            registerModule(AfterburnerModule())
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }.also { bind(ObjectMapper::class.java).toInstance(it) }

        DataTypeResolver.scan()

        ClassGraph().acceptPaths("data").scan().use {
            val (keyed, other) = (it.allResources.map { it.url } + path.walk().filter { it.isFile }.map { it.toURI().toURL() })
                .mapNotNull {
                    when (it.file.substring(it.file.lastIndexOf('.') + 1)) {
                        "json" -> {
                            @Suppress("UNCHECKED_CAST")
                            objectMapper.readValue<Data>(it)
                        }
                        else -> null
                    }
                }
                .partition { it is KeyedData }
            keyed.filterIsInstance<KeyedData>()
                .groupBy { DataTypeResolver.typeByClass(it::class) }
                .forEach { (_, value) ->
                    @Suppress("UNCHECKED_CAST")
                    (bind(TypeLiteral.get(Types.mapOf(String::class.java, value.first()::class.java))) as AnnotatedBindingBuilder<Any>).toInstance(value.associateBy { it.key })
                }
            other.forEach {
                @Suppress("UNCHECKED_CAST")
                (bind(it::class.java) as AnnotatedBindingBuilder<Any>).toInstance(it)
            }
        }
    }
}
