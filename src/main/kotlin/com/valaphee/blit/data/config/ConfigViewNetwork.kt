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

package com.valaphee.blit.data.config

import com.valaphee.blit.data.locale.Locale
import com.valaphee.blit.source.IntStringConverter
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import tornadofx.Fragment
import tornadofx.bind
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.form
import tornadofx.hgrow
import tornadofx.isInt
import tornadofx.label
import tornadofx.passwordfield
import tornadofx.radiobutton
import tornadofx.selectedValueProperty
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
class ConfigViewNetwork : Fragment("Network") {
    private val locale by di<Locale>()
    private val configModel by di<Config.Model>()

    override val root = form {
        fieldset(locale["config.network.proxy.text"]) {
            val proxyMode = ToggleGroup()
            radiobutton("None", proxyMode, Config.ProxyMode.None)
            radiobutton("System", proxyMode, Config.ProxyMode.System)
            val manualProxyMode = radiobutton("Manual", proxyMode, Config.ProxyMode.Manual)
            proxyMode.selectedValueProperty<Config.ProxyMode>().bindBidirectional(configModel.proxyMode)
            field("Host") {
                enableWhen(manualProxyMode.selectedProperty())
                textfield(configModel.proxyHost) { hgrow = Priority.ALWAYS }
                label("Port")
                textfield {
                    bind(configModel.proxyPort, converter = IntStringConverter)

                    minWidth = 65.0
                    maxWidth = 65.0

                    filterInput { it.controlNewText.isInt() }
                }
            }
            field("Username") {
                enableWhen(manualProxyMode.selectedProperty())
                textfield(configModel.proxyUsername)
            }
            field("Password") {
                enableWhen(manualProxyMode.selectedProperty())
                passwordfield(configModel.proxyPassword)
            }
        }
    }
}
