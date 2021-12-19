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
import com.valaphee.blit.source.Source
import com.valaphee.blit.source.SourceUi
import com.valaphee.blit.source.dav.DavSourceUi
import com.valaphee.blit.source.k8scp.K8scpSourceUi
import com.valaphee.blit.source.local.LocalSourceUi
import com.valaphee.blit.source.scp.ScpSourceUi
import com.valaphee.blit.source.sftp.SftpSourceUi
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority
import tornadofx.Field
import tornadofx.Fragment
import tornadofx.action
import tornadofx.asObservable
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.combobox
import tornadofx.dynamicContent
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.listview
import tornadofx.onChange
import tornadofx.vbox
import tornadofx.vgrow

/**
 * @author Kevin Ludwig
 */
class ConfigViewSources : Fragment("Sources") {
    private val locale by di<Locale>()
    private val configModel by di<ConfigModel>()

    private val source = SimpleObjectProperty<Source<*>>().apply {
        onChange {
            it?.let {
                type.value = null
                type.value = sourceUis.first { sourceUi -> sourceUi.`class` == it::class }
            }
        }
    }
    private val type = SimpleObjectProperty<SourceUi>()
    private lateinit var fields: List<Field>

    override val root = hbox {
        vbox {
            val sources = listview(configModel.sources) {
                bindSelected(source)

                vgrow = Priority.ALWAYS

                selectionModel.selectFirst()
            }
            add(sources)
            hbox {
                button(locale["config.sources.new.text"]) {
                    action {
                        sources.selectionModel.select(null)
                        type.value = null
                    }
                }
                button(locale["config.sources.save.text"]) {
                    action {
                        val source = type.value.getSource(fields)!!
                        if (sources.selectionModel.selectedIndex != -1) configModel.sources[sources.selectionModel.selectedIndex] = source
                        else {
                            configModel.sources.add(source)
                            sources.selectionModel.select(source)
                        }
                    }
                }
                button(locale["config.sources.delete.text"]) {
                    action {
                        configModel.sources.remove(source.value)
                        sources.selectionModel.selectFirst()
                    }
                }
            }
        }
        form {
            hgrow = Priority.ALWAYS

            fieldset { field("Type") { combobox(type, sourceUis.toList().asObservable()) { cellFormat { text = locale["config.sources.${it.javaClass.simpleName}"] } } } }
            fieldset { dynamicContent(type) { it?.let { it.getFields(this, source.value).also { fields = it } } } }
        }
    }

    companion object {
        private val sourceUis = setOf(DavSourceUi, K8scpSourceUi, LocalSourceUi, ScpSourceUi, SftpSourceUi)
    }
}
