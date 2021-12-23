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

package com.valaphee.blit.source.k8scp

import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.source.SourceConfig
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import tornadofx.field
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("k8scp")
class K8scpSourceConfig(
    name: String,
    namespace: String = "",
    pod: String = "",
) : SourceConfig(name) {
    private val namespaceProperty = SimpleStringProperty(namespace)
    var namespace: String by namespaceProperty

    private val podProperty = SimpleStringProperty(pod)
    var pod: String by podProperty

    override fun newUi(eventTarget: EventTarget) {
        with(eventTarget) {
            field("Name") { textfield(nameProperty) }
            field("Namespace") { textfield(namespaceProperty) }
            field("Pod") { textfield(podProperty) }
        }
    }

    override fun newSource() = K8scpSource(nameProperty.value, namespaceProperty.value, podProperty.value)
}
