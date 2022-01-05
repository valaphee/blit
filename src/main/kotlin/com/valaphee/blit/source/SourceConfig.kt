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

package com.valaphee.blit.source

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.valaphee.blit.source.dav.DavSourceConfig
import com.valaphee.blit.source.ftp.FtpSourceConfig
import com.valaphee.blit.source.k8scp.K8scpSourceConfig
import com.valaphee.blit.source.local.LocalSourceConfig
import com.valaphee.blit.source.scp.ScpSourceConfig
import com.valaphee.blit.source.sftp.SftpSourceConfig
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import tornadofx.getValue
import tornadofx.setValue

/**
 * @author Kevin Ludwig
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(DavSourceConfig::class),
    JsonSubTypes.Type(FtpSourceConfig::class),
    JsonSubTypes.Type(K8scpSourceConfig::class),
    JsonSubTypes.Type(LocalSourceConfig::class),
    JsonSubTypes.Type(ScpSourceConfig::class),
    JsonSubTypes.Type(SftpSourceConfig::class)
)
abstract class SourceConfig(
    name: String
) {
    @get:JsonIgnore protected val nameProperty = SimpleStringProperty(name)
    var name: String by nameProperty

    abstract fun newUi(eventTarget: EventTarget)

    abstract fun newSource(): Source<*>

    override fun toString() = name
}
