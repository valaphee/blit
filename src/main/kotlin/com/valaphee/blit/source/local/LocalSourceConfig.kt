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

package com.valaphee.blit.source.local

import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.source.SourceConfig
import com.valaphee.blit.source.k8scp.K8scpSourceConfig
import javafx.event.EventTarget
import tornadofx.field
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("local")
class LocalSourceConfig(
    name: String
) : SourceConfig(name) {
    override fun newUi(eventTarget: EventTarget) {
        with(eventTarget) {
            field("Name") { textfield(nameProperty) }
        }
    }

    override fun newSource() = LocalSource(nameProperty.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as K8scpSourceConfig

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()
}
