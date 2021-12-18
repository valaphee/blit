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

package com.valaphee.blit.source.dav

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.util.Date

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
                @get:JacksonXmlProperty(namespace = "DAV:", localName = "getlastmodified") val getlastmodified: Date? = null,
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
