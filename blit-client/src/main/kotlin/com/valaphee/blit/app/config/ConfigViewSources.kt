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

package com.valaphee.blit.app.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.valaphee.blit.Source
import com.valaphee.blit.k8scp.K8scpSource
import com.valaphee.blit.k8scp.K8scpSourceUi
import com.valaphee.blit.local.LocalSource
import com.valaphee.blit.local.LocalSourceUi
import com.valaphee.blit.sftp.SftpSource
import com.valaphee.blit.sftp.SftpSourceUi
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import tornadofx.Field
import tornadofx.Fragment
import tornadofx.action
import tornadofx.asObservable
import tornadofx.bindSelected
import tornadofx.button
import tornadofx.buttonbar
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
import java.io.File

/**
 * @author Kevin Ludwig
 */
class ConfigViewSources : Fragment("Sources") {
    private val _config by di<Config>()
    private val objectMapper by di<ObjectMapper>()

    private val source = SimpleObjectProperty<Source<*>>().apply {
        onChange {
            it?.let {
                type.value = null
                type.value = when (it) {
                    is K8scpSource -> "k8scp"
                    is LocalSource -> "local"
                    is SftpSource -> "sftp"
                    else -> TODO(it::class.java.name)
                }
            }
        }
    }
    private val sources = listview(_config.sources) {
        vgrow = Priority.ALWAYS

        bindSelected(source)
    }
    private val type = SimpleStringProperty()
    private lateinit var fields: List<Field>

    override val root = hbox {
        vbox {
            add(sources)
            buttonbar {
                button("New") {
                    action {
                        sources.selectionModel.select(null)
                        type.value = null
                    }
                }
                button("Save") {
                    action {
                        val source = sourceUis[type.value]!!.getSource(fields)!!
                        if (sources.selectionModel.selectedIndex != -1) _config.sources[sources.selectionModel.selectedIndex] = source
                        else {
                            _config.sources.add(source)
                            sources.selectionModel.select(source)
                        }
                        objectMapper.writeValue(File("config.json"), _config)
                    }
                }
                button("Delete") {
                    action {
                        _config.sources.remove(source.value)
                        sources.selectionModel.selectFirst()
                        objectMapper.writeValue(File("config.json"), _config)
                    }
                }
            }
        }
        form {
            hgrow = Priority.ALWAYS

            fieldset(labelPosition = Orientation.VERTICAL) { field("Type") { combobox<String>(type) { items = sourceUis.keys.toList().asObservable() } } }
            fieldset(labelPosition = Orientation.VERTICAL) { dynamicContent(type) { it?.let { sourceUis[it]!!.getFields(this, source.value).also { fields = it } } } }
        }

        sources.selectionModel.selectFirst()
    }

    companion object {
        private val sourceUis = mutableMapOf(
            "k8scp" to K8scpSourceUi,
            "local" to LocalSourceUi,
            "sftp" to SftpSourceUi
        )
    }
}
