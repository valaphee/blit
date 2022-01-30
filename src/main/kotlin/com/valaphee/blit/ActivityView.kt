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

import com.valaphee.blit.config.Config
import com.valaphee.blit.locale.Locale
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import jfxtras.styles.jmetro.JMetroStyleClass
import tornadofx.Stylesheet
import tornadofx.View
import tornadofx.addClass
import tornadofx.progressbar
import tornadofx.readonlyColumn
import tornadofx.stackpane
import tornadofx.stringBinding
import tornadofx.tableview
import tornadofx.text
import tornadofx.useMaxSize
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
                MainView.tableColumnBaseSetWidth(this, 549.0)
                isReorderable = false
            }
            readonlyColumn("", Activity.Task::progressProperty) {
                MainView.tableColumnBaseSetWidth(this, 250.0)
                isReorderable = false

                cellFormat {
                    addClass(Stylesheet.progressBarTableCell)
                    graphic = stackpane {
                        progressbar(it) { useMaxSize = true }
                        text(it.stringBinding { it?.let { if (it.toDouble() < 0.0) null else progressFormat.format(it) } })
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
