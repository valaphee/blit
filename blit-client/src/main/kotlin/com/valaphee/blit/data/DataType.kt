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

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * @author Kevin Ludwig
 */
@Target(AnnotationTarget.CLASS)
annotation class DataType(
    val value: String
)

/**
 * @author Kevin Ludwig
 */
object DataTypeResolver : TypeIdResolverBase() {
    private val classByType = mutableMapOf<String, KClass<*>>()
    private val typeByClass = mutableMapOf<KClass<*>, String>()

    fun scan() {
        ClassGraph().enableClassInfo().enableAnnotationInfo().scan().use {
            val dataType = DataType::class.jvmName
            classByType.putAll(it.getClassesWithAnnotation(dataType).associate { it.getAnnotationInfo(dataType).parameterValues.getValue("value") as String to Class.forName(it.name).kotlin })
            typeByClass.putAll(classByType.toList().associate { it.second to it.first })
        }
    }

    private fun classByType(type: String) = classByType[type] ?: throw UnknownDataTypeException(type)

    fun typeByClass(`class`: KClass<*>) = typeByClass[`class`] ?: throw UnknownDataTypeException(`class`.jvmName)

    override fun idFromValue(value: Any) = typeByClass(value::class)

    override fun idFromValueAndType(value: Any, suggestedType: Class<*>) = idFromValue(value)

    override fun typeFromId(context: DatabindContext, key: String): JavaType = context.constructType(classByType(key).java)

    override fun getMechanism() = JsonTypeInfo.Id.NAME
}
