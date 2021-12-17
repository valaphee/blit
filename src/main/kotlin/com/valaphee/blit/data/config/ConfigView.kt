/*
 * MIT License
 *
 * Copyright (c) 2021, Valaphee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.valaphee.blit.data.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.valaphee.blit.data.locale.Locale
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.tab
import tornadofx.tabpane
import tornadofx.vbox
import tornadofx.vgrow
import java.io.File

/**
 * @author Kevin Ludwig
 */
class ConfigView : View("Configure Blit") {
    private val locale by di<Locale>()
    private val _config by di<Config>()
    private val objectMapper by di<ObjectMapper>()

    override val root = vbox {
        JMetro(this, Style.DARK)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        prefWidth = 800.0
        prefHeight = 600.0

        tabpane {
            vgrow = Priority.ALWAYS
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            tab(ConfigViewGeneral::class)
            tab(ConfigViewSources::class)
        }
        buttonbar {
            button(locale["config.ok.text"]) {
                action {
                    objectMapper.writeValue(File(File("data").also(File::mkdir), "config.json"), _config)
                    (scene.window as Stage).close()
                }
            }
            button(locale["config.cancel.text"]) { action { (scene.window as Stage).close() } }
            button(locale["config.apply.text"]) { action { objectMapper.writeValue(File(File("data").also(File::mkdir), "config.json"), _config) } }
        }
    }
}
