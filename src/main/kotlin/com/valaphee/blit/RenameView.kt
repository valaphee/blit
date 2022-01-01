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

package com.valaphee.blit

import com.valaphee.blit.data.config.Config
import com.valaphee.blit.data.locale.Locale
import javafx.beans.property.SimpleStringProperty
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetroStyleClass
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
class RenameView(
    name: String,
    action: (String) -> Unit
) : View("Rename $name") {
    private val locale by di<Locale>()
    private val _config by di<Config>()

    private val name = SimpleStringProperty(name)

    override val root = form {
        _config.theme.apply(this)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        prefWidth = 300.0

        fieldset { field { textfield(this@RenameView.name) } }
        buttonbar {
            button(locale["rename.ok.text"]) {
                enableWhen(this@RenameView.name.isNotEmpty)
                action {
                    action(this@RenameView.name.value)
                    (scene.window as Stage).close()
                }
            }
            button(locale["rename.cancel.text"]) { action { (scene.window as Stage).close() } }
        }
    }
}
