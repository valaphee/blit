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

package com.valaphee.blit.source

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.valaphee.blit.source.dav.DavSource
import com.valaphee.blit.source.k8scp.K8scpSource
import com.valaphee.blit.source.local.LocalSource
import com.valaphee.blit.source.sftp.SftpSource

/**
 * @author Kevin Ludwig
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(DavSource::class),
    JsonSubTypes.Type(K8scpSource::class),
    JsonSubTypes.Type(LocalSource::class),
    JsonSubTypes.Type(SftpSource::class)
)
interface Source<T : Entry<T>> {
    @get:JsonProperty("name") val name: String
    @get:JsonIgnore val home: String

    suspend fun isValid(path: String): Boolean

    suspend fun get(path: String): T
}
