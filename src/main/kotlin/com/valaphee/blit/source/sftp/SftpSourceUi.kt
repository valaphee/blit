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

package com.valaphee.blit.source.sftp

import com.valaphee.blit.source.Source
import com.valaphee.blit.source.SourceUi
import javafx.event.EventTarget
import javafx.scene.control.TextField
import tornadofx.Field
import tornadofx.field
import tornadofx.filterInput
import tornadofx.isInt
import tornadofx.passwordfield
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
object SftpSourceUi : SourceUi {
    override val name get() = "SFTP"
    override val `class` get() = SftpSource::class

    override fun getFields(eventTarget: EventTarget, source: Source<*>?) = with(eventTarget) {
        val sftpSource = source as? SftpSource
        listOf(
            field("Name") { textfield(source?.name ?: "") },
            field("Address") {
                textfield(sftpSource?.host ?: "")
                textfield(sftpSource?.port?.toString() ?: "") { filterInput { it.controlNewText.isInt() } }
            },
            field("Auth") {
                textfield(sftpSource?.username ?: "")
                passwordfield(sftpSource?.password ?: "")
            }
        )
    }

    override fun getSource(fields: List<Field>) = SftpSource(
        (fields[0].inputs[0] as TextField).text,
        (fields[1].inputs[0] as TextField).text,
        (fields[1].inputs[1] as TextField).text.toInt(),
        (fields[2].inputs[0] as TextField).text,
        (fields[2].inputs[1] as TextField).text
    )
}
