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

package com.valaphee.blit.dav

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * @author Kevin Ludwig
 */
@JacksonXmlRootElement(namespace = "DAV:", localName = "multistatus")
data class Multistatus(
    @get:JacksonXmlElementWrapper(namespace = "DAV:", localName = "response", useWrapping = false) val response: List<Response>,
) {
    class Response(
        @get:JacksonXmlProperty(namespace = "DAV:", localName = "href") val href: String,
        @get:JacksonXmlElementWrapper(namespace = "DAV:", localName = "propstat", useWrapping = false) val propstat: List<Propstat>,
    ) {
        class Propstat(
            @get:JacksonXmlProperty(namespace = "DAV:", localName = "prop") val prop: Prop,
            @get:JacksonXmlProperty(namespace = "DAV:", localName = "status") val status: String
        ) {
            class Prop(
                @get:JacksonXmlProperty(namespace = "DAV:", localName = "getlastmodified") val getlastmodified: String? = null,
                @get:JacksonXmlProperty(namespace = "DAV:", localName = "getetag") val getetag: String? = null,
                @get:JacksonXmlProperty(namespace = "DAV:", localName = "getcontenttype") val getcontenttype: String? = null,
                @get:JacksonXmlProperty(namespace = "DAV:", localName = "resourcetype") val resourcetype: Resourcetype? = null,
                @get:JacksonXmlProperty(namespace = "DAV:", localName = "getcontentlength") val getcontentlength: Long = 0
            ) {
                class Resourcetype(
                    @get:JacksonXmlProperty(namespace = "DAV:", localName = "collection") val collection: Collection? = null
                ) {
                    class Collection
                }
            }
        }
    }
}
