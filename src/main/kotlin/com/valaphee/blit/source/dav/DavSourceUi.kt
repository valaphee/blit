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

import com.valaphee.blit.source.Source
import com.valaphee.blit.source.SourceUi
import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import tornadofx.Field
import tornadofx.checkbox
import tornadofx.field
import tornadofx.filterInput
import tornadofx.isLong
import tornadofx.passwordfield
import tornadofx.textfield
import tornadofx.toProperty

/**
 * @author Kevin Ludwig
 */
object DavSourceUi : SourceUi {
    override val `class` get() = DavSource::class

    override fun getFields(eventTarget: EventTarget, source: Source<*>?) = with(eventTarget) {
        val davSource = source as? DavSource
        listOf(
            field("Name") { textfield(source?.name ?: "") },
            field("Url") { textfield(davSource?.url ?: "") },
            field("Username") { textfield(davSource?.username ?: "") },
            field("Password") { passwordfield(davSource?.password ?: "") },
            field("Nextcloud") { checkbox(property = davSource?.nextcloud.toProperty()) },
            field("Nextcloud Upload Chunk Size") { textfield(davSource?.nextcloudUploadChunkSize?.toString() ?: "") { filterInput { it.controlNewText.isLong() } } }
        )
    }

    override fun getSource(fields: List<Field>) = DavSource(
        (fields[0].inputs[0] as TextField).text,
        (fields[1].inputs[0] as TextField).text,
        (fields[2].inputs[0] as TextField).text,
        (fields[3].inputs[0] as TextField).text,
        (fields[4].inputs[0] as CheckBox).isSelected,
        (fields[5].inputs[0] as TextField).text.toLong()
    )
}
