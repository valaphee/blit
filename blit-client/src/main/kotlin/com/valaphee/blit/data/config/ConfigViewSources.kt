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

import com.valaphee.blit.data.locale.Locale
import com.valaphee.blit.source.Source
import com.valaphee.blit.source.dav.DavSourceUi
import com.valaphee.blit.source.k8scp.K8scpSourceUi
import com.valaphee.blit.source.local.LocalSourceUi
import com.valaphee.blit.source.sftp.SftpSourceUi
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
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
    private val _config by di<Config>()

    private val source = SimpleObjectProperty<Source<*>>().apply {
        onChange {
            it?.let {
                type.value = null
                type.value = sourceUis.values.first { sourceUi -> sourceUi.`class` == it::class }.name
            }
        }
    }
    private val type = SimpleStringProperty()
    private lateinit var fields: List<Field>

    override val root = hbox {
        vbox {
            val sources = listview(_config.sources) {
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
                        val source = sourceUis[type.value]!!.getSource(fields)!!
                        if (sources.selectionModel.selectedIndex != -1) _config.sources[sources.selectionModel.selectedIndex] = source
                        else {
                            _config.sources.add(source)
                            sources.selectionModel.select(source)
                        }
                    }
                }
                button(locale["config.sources.delete.text"]) {
                    action {
                        _config.sources.remove(source.value)
                        sources.selectionModel.selectFirst()
                    }
                }
            }
        }
        form {
            hgrow = Priority.ALWAYS

            fieldset { field("Type") { combobox<String>(type, sourceUis.keys.toList().asObservable()) } }
            fieldset { dynamicContent(type) { it?.let { sourceUis[it]!!.getFields(this, source.value).also { fields = it } } } }
        }
    }

    companion object {
        private val sourceUis = setOf(DavSourceUi, K8scpSourceUi, LocalSourceUi, SftpSourceUi).associateBy { it.name }
    }
}