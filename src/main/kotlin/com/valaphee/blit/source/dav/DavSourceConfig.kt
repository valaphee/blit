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

import com.valaphee.blit.source.SourceConfig
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import tornadofx.checkbox
import tornadofx.field
import tornadofx.filterInput
import tornadofx.isLong
import tornadofx.passwordfield
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
class DavSourceConfig(
    name: String,
    url: String = "",
    username: String = "",
    password: String = "",
    nextcloud: Boolean = false,
    nextcloudUploadChunkSize: Long = 10L * 1024 * 1024
) : SourceConfig(name) {
    val urlProperty = SimpleStringProperty(url)
    val usernameProperty = SimpleStringProperty(username)
    val passwordProperty = SimpleStringProperty(password)
    val nextcloudProperty = SimpleBooleanProperty(nextcloud)
    val nextcloudUploadChunkSizeProperty = SimpleLongProperty(nextcloudUploadChunkSize)

    override fun newUi(eventTarget: EventTarget) {
        with(eventTarget) {
            field("Name") { textfield(nameProperty) }
            field("Url") { textfield(urlProperty) }
            field("Username") { textfield(usernameProperty) }
            field("Password") { passwordfield(passwordProperty) }
            field("Nextcloud") { checkbox(property = nextcloudProperty) }
            field("Nextcloud Upload Chunk Size") { textfield(nextcloudUploadChunkSizeProperty) { filterInput { it.controlNewText.isLong() } } }
        }
    }

    override fun newSource() = DavSource(nameProperty.value, urlProperty.value, usernameProperty.value, passwordProperty.value, nextcloudProperty.value, nextcloudUploadChunkSizeProperty.value)
}
