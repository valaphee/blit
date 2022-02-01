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

package com.valaphee.blit.config

import com.valaphee.blit.locale.Locale
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetroStyleClass
import tornadofx.UIComponent
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.enableWhen
import tornadofx.select
import tornadofx.tab
import tornadofx.tabpane
import tornadofx.vbox
import tornadofx.vgrow
import kotlin.reflect.KClass

/**
 * An [ConfigView] is an ui which is used to modify the [Config] it is split into different categories.
 *
 * @author Kevin Ludwig
 */
class ConfigView : View("Configure Blit") {
    private val locale by di<Locale>()
    private val _config by di<Config>()
    private val configModel by di<Config.Model>()

    // Classes of tabs and their specific JavaFX counterpart, used for selection.
    private var tabs = mutableMapOf<KClass<out UIComponent>, Tab>()

    override val root = vbox {
        _config.theme.apply(this)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        setPrefSize(800.0, 600.0)

        tabpane {
            vgrow = Priority.ALWAYS
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            tab(ConfigViewGeneral::class)
            tab(ConfigViewSources::class)
            tab(ConfigViewNetwork::class)
        }
        buttonbar {
            button(locale["config.ok.text"]) {
                action {
                    configModel.commit()
                    (scene.window as Stage).close()
                }
            }
            button(locale["config.cancel.text"]) {
                action {
                    configModel.rollback()
                    (scene.window as Stage).close()
                }
            }
            button(locale["config.apply.text"]) {
                enableWhen(configModel.dirty)
                action(configModel::commit)
            }
        }
    }

    /**
     * Selects a specific tab by its class.
     *
     * @param `class` Class of the tab to select
     */
    fun <T : UIComponent> select(`class`: KClass<out T>) {
        tabs[`class`]?.select()
    }

    /**
     * Selects a specific tab by its class.
     *
     * @param T the class of the tab to select.
     */
    inline fun <reified T : UIComponent> select() = select(T::class)

    private fun TabPane.tab(uiComponent: KClass<out UIComponent>, op: Tab.() -> Unit = {}) = tab(find(uiComponent), op).also { this@ConfigView.tabs[uiComponent] = it }
}
