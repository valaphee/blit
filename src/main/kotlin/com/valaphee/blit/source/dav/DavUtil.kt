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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.HttpMethod

internal val httpMethodPropfind = HttpMethod("PROPFIND")
internal val httpMethodMkcol = HttpMethod("MKCOL")
internal val httpMethodMove = HttpMethod("MOVE")
internal val xmlMapper = XmlMapper().apply {
    registerModule(AfterburnerModule())
    registerKotlinModule()

    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}
