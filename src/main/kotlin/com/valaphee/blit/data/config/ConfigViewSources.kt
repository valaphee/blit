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
import com.valaphee.blit.source.SourceConfig
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ContextMenu
import javafx.scene.layout.Priority
import tornadofx.Fragment
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.dynamicContent
import tornadofx.form
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.item
import tornadofx.listview
import tornadofx.onChange
import tornadofx.vbox
import tornadofx.vgrow

/**
 * @author Kevin Ludwig
 */
class ConfigViewSources : Fragment("Sources") {
    private val locale by di<Locale>()
    private val configModel by di<Config.Model>()

    private val source = SimpleObjectProperty<SourceConfig>()

    override val root = hbox {
        vbox {
            add(listview(configModel.sources) {
                bindSelected(source)

                vgrow = Priority.ALWAYS

                selectionModel.selectedItems.onChange { contextMenu = ContextMenu().apply { it.list.firstOrNull()?.let { item(locale["config.sources.delete.text"]) { action { configModel.sources.remove(it) } } } ?: item(locale["config.sources.new.text"]) { action {} } } }
            })
        }
        form {
            hgrow = Priority.ALWAYS

            dynamicContent(source) { source.value?.newUi(this) }
        }
    }
}
