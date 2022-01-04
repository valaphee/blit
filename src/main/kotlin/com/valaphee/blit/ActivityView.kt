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
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import jfxtras.styles.jmetro.JMetroStyleClass
import tornadofx.View
import tornadofx.fixedWidth
import tornadofx.onChange
import tornadofx.progressbar
import tornadofx.readonlyColumn
import tornadofx.stackpane
import tornadofx.tableview
import tornadofx.text
import tornadofx.vbox
import tornadofx.vgrow
import java.text.NumberFormat

/**
 * @author Kevin Ludwig
 */
class ActivityView : View("Activity") {
    private val locale by di<Locale>()
    private val _config by di<Config>()

    private val activity by di<Activity>()

    override val root = vbox {
        _config.theme.apply(this)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        prefWidth = 800.0

        tableview(activity.tasks) {
            vgrow = Priority.ALWAYS

            placeholder = Label("")

            readonlyColumn("", Activity.Task::name) {
                fixedWidth(250)
                isReorderable = false
            }
            readonlyColumn("Progress", Activity.Task::progressProperty) {
                fixedWidth(125)
                isReorderable = false

                cellFormat {
                    graphic = stackpane {
                        progressbar(it)
                        text().apply { it.onChange { text = progressFormat.format(it) } }
                    }
                }
            }

            setSortPolicy { false }
        }
    }

    companion object {
        private val progressFormat = NumberFormat.getPercentInstance().apply { minimumFractionDigits = 1 }
    }
}
