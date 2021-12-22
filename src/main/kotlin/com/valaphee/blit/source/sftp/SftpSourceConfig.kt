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

import com.valaphee.blit.source.SourceConfig
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import tornadofx.action
import tornadofx.button
import tornadofx.chooseFile
import tornadofx.field
import tornadofx.filterInput
import tornadofx.isInt
import tornadofx.passwordfield
import tornadofx.textfield
import java.io.File

/**
 * @author Kevin Ludwig
 */
class SftpSourceConfig(
    name: String,
    host: String = "",
    port: Int = 22,
    username: String = "",
    password: String = "",
    privateKey: String = "",
    connectionPoolSize: Int = 4
) : SourceConfig(name) {
    val hostProperty = SimpleStringProperty(host)
    val portProperty = SimpleIntegerProperty(port)
    val usernameProperty = SimpleStringProperty(username)
    val passwordProperty = SimpleStringProperty(password)
    val privateKeyProperty = SimpleStringProperty(privateKey)
    val connectionPoolSizeProperty = SimpleIntegerProperty(connectionPoolSize)

    override fun newUi(eventTarget: EventTarget) {
        with(eventTarget) {
            field("Name") { textfield(nameProperty) }
            field("Host") { textfield(hostProperty) }
            field("Port") { textfield(portProperty) { filterInput { it.controlNewText.isInt() } } }
            field("Username") { textfield(usernameProperty) }
            field("Password") { passwordfield(passwordProperty) }
            field("Private Key") {
                val path = textfield(privateKeyProperty)
                button("...") {
                    action {
                        val parentPath = if (path.text.isEmpty()) null else File(path.text).parentFile
                        chooseFile("Select Private Key", emptyArray(), if (parentPath?.isDirectory == true) parentPath else null).firstOrNull()?.let { path.text = it.absolutePath }
                    }
                }
            }
        }
    }

    override fun newSource() = SftpSource(nameProperty.value, hostProperty.value, portProperty.value, usernameProperty.value, passwordProperty.value, privateKeyProperty.value, connectionPoolSizeProperty.value)
}
