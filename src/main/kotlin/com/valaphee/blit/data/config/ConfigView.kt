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

package com.valaphee.blit.data.config

import com.valaphee.blit.data.locale.Locale
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import tornadofx.Component
import tornadofx.Fragment
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.select
import tornadofx.tab
import tornadofx.tabpane
import tornadofx.vbox
import tornadofx.vgrow
import kotlin.reflect.KClass

/**
 * @author Kevin Ludwig
 */
class ConfigView : View("Configure Blit") {
    private val locale by di<Locale>()
    private val _config by di<Config>()

    private var tabs = mutableMapOf<KClass<out Component>, Tab>()

    override val root = vbox {
        JMetro(this, Style.DARK)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        prefWidth = 800.0
        prefHeight = 600.0

        tabpane {
            vgrow = Priority.ALWAYS
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            this@ConfigView.tabs[ConfigViewGeneral::class] = tab(ConfigViewGeneral::class)
            this@ConfigView.tabs[ConfigViewSources::class] = tab(ConfigViewSources::class)
        }
        buttonbar {
            button(locale["config.ok.text"]) {
                action {
                    //_config.commit()
                    (scene.window as Stage).close()
                }
            }
            button(locale["config.cancel.text"]) {
                action {
                    //_config.rollback()
                    (scene.window as Stage).close()
                }
            }
            button(locale["config.apply.text"]) {
                //enableWhen(_config.dirty)
                //action(_config::commit)
            }
        }
    }

    fun <T : Fragment> select(`class`: KClass<out T>) {
        tabs[`class`]?.select()
    }

    inline fun <reified T> select() {
        @Suppress("UNCHECKED_CAST")
        select(T::class as KClass<Fragment>)
    }
}
