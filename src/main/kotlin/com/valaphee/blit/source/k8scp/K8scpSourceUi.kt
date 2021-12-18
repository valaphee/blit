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

import com.valaphee.blit.source.Source
import com.valaphee.blit.source.SourceUi
import javafx.event.EventTarget
import javafx.scene.control.TextField
import tornadofx.Field
import tornadofx.field
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
object K8scpSourceUi : SourceUi {
    override val name get() = "K8s CP"
    override val `class` get() = K8scpSource::class

    override fun getFields(eventTarget: EventTarget, source: Source<*>?) = with(eventTarget) {
        val k8scpSource = source as? K8scpSource
        listOf(
            field("Name") { textfield(source?.name ?: "") },
            field ("Namespace") { textfield(k8scpSource?.namespace ?: "") },
            field ("Pod") { textfield(k8scpSource?.pod ?: "") }
        )
    }

    override fun getSource(fields: List<Field>) = K8scpSource(
        (fields[0].inputs[0] as TextField).text,
        (fields[1].inputs[0] as TextField).text,
        (fields[2].inputs[0] as TextField).text
    )
}
