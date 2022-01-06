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
import com.valaphee.blit.source.dav.DavSourceConfig
import com.valaphee.blit.source.k8scp.K8scpSourceConfig
import com.valaphee.blit.source.local.LocalSourceConfig
import com.valaphee.blit.source.scp.ScpSourceConfig
import com.valaphee.blit.source.sftp.SftpSourceConfig
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
 * Sources configuration tab
 *
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

                /*setCellFactory {
                    ListCell<SourceConfig>().apply {
                        setOnMouseClicked {
                            if (isEmpty) selectionModel.clearSelection()

                            it.consume()
                        }
                    }
                } FIXME: Text is invisible*/

                selectionModel.selectedItems.onChange {
                    contextMenu = ContextMenu().apply {
                        it.list.firstOrNull()?.let { item(locale["config.sources.delete.text"]) { action { configModel.sources.remove(it) } } } ?: run {
                            item(locale["config.sources.new.text", locale["config.sources.type.dav"]]) {
                                action {
                                    configModel.sources.add(DavSourceConfig(locale["config.sources.new.text", locale["config.sources.type.dav"]]))
                                    selectionModel.selectLast()
                                }
                            }
                            item(locale["config.sources.new.text", locale["config.sources.type.k8scp"]]) {
                                action {
                                    configModel.sources.add(K8scpSourceConfig(locale["config.sources.new.text", locale["config.sources.type.k8scp"]]))
                                    selectionModel.selectLast()
                                }
                            }
                            item(locale["config.sources.new.text", locale["config.sources.type.local"]]) {
                                action {
                                    configModel.sources.add(LocalSourceConfig(locale["config.sources.new.text", locale["config.sources.type.local"]]))
                                    selectionModel.selectLast()
                                }
                            }
                            item(locale["config.sources.new.text", locale["config.sources.type.scp"]]) {
                                action {
                                    configModel.sources.add(ScpSourceConfig(locale["config.sources.new.text", locale["config.sources.type.scp"]]))
                                    selectionModel.selectLast()
                                }
                            }
                            item(locale["config.sources.new.text", locale["config.sources.type.sftp"]]) {
                                action {
                                    configModel.sources.add(SftpSourceConfig(locale["config.sources.new.text", locale["config.sources.type.sftp"]]))
                                    selectionModel.selectLast()
                                }
                            }
                        }
                    }
                }
            })
        }
        form {
            hgrow = Priority.ALWAYS

            dynamicContent(source) { source.value?.newUi(this) }
        }
    }
}
