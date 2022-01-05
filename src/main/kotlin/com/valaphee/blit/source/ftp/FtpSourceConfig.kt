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

package com.valaphee.blit.source.ftp

import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.source.IntStringConverter
import com.valaphee.blit.source.SourceConfig
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.layout.Priority
import tornadofx.bind
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.getValue
import tornadofx.hgrow
import tornadofx.isInt
import tornadofx.label
import tornadofx.passwordfield
import tornadofx.separator
import tornadofx.setValue
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("ftp")
class FtpSourceConfig(
    name: String,
    host: String = "",
    port: Int = 22,
    username: String = "",
    password: String = "",
    connectionPoolSize: Int = 4
) : SourceConfig(name) {
    private val hostProperty = SimpleStringProperty(host)
    var host: String by hostProperty

    private val portProperty = SimpleIntegerProperty(port)
    var port: Int by portProperty

    private val usernameProperty = SimpleStringProperty(username)
    var username: String by usernameProperty

    private val passwordProperty = SimpleStringProperty(password)
    var password: String by passwordProperty

    private val connectionPoolSizeProperty = SimpleIntegerProperty(connectionPoolSize)
    var connectionPoolSize: Int by connectionPoolSizeProperty

    override fun newUi(eventTarget: EventTarget) {
        with(eventTarget) {
            fieldset("General") {
                field("Name") { textfield(nameProperty) }
                field("Host") {
                    textfield(hostProperty) { hgrow = Priority.ALWAYS }
                    label("Port")
                    textfield {
                        bind(portProperty, converter = IntStringConverter)

                        minWidth = 65.0
                        maxWidth = 65.0

                        filterInput { it.controlNewText.isInt() }
                    }
                }
                field("Connection Pool Size") {
                    textfield {
                        bind(connectionPoolSizeProperty, converter = IntStringConverter)

                        filterInput { it.controlNewText.isInt() }
                    }
                }
                separator()
                field("Username") { textfield(usernameProperty) }
                field("Password") { passwordfield(passwordProperty) }
            }
        }
    }

    override fun newSource() = FtpSource(hostProperty.value, portProperty.value, usernameProperty.value, passwordProperty.value, connectionPoolSizeProperty.value)
}
