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

package com.valaphee.blit.source.scp

import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.source.SourceConfig
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import tornadofx.action
import tornadofx.button
import tornadofx.chooseFile
import tornadofx.field
import tornadofx.filterInput
import tornadofx.getValue
import tornadofx.isInt
import tornadofx.passwordfield
import tornadofx.setValue
import tornadofx.textfield
import java.io.File

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("scp")
class ScpSourceConfig(
    name: String,
    host: String = "",
    port: Int = 22,
    username: String = "",
    password: String = "",
    privateKey: String = "",
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

    private val privateKeyProperty = SimpleStringProperty(privateKey)
    var privateKey: String by privateKeyProperty

    private val connectionPoolSizeProperty = SimpleIntegerProperty(connectionPoolSize)
    var connectionPoolSize: Int by connectionPoolSizeProperty

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

    override fun newSource() = ScpSource(nameProperty.value, hostProperty.value, portProperty.value, usernameProperty.value, passwordProperty.value, privateKeyProperty.value, connectionPoolSizeProperty.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScpSourceConfig

        if (name != other.name) return false
        if (host != other.host) return false
        if (port != other.port) return false
        if (username != other.username) return false
        if (password != other.password) return false
        if (privateKey != other.privateKey) return false
        if (connectionPoolSize != other.connectionPoolSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + host.hashCode()
        result = 31 * result + port.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + privateKey.hashCode()
        result = 31 * result + connectionPoolSize.hashCode()
        return result
    }
}
