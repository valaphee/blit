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

package com.valaphee.tead.transfer

import com.valaphee.tead.Config
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import org.controlsfx.control.BreadCrumbBar
import tornadofx.Dimension
import tornadofx.View
import tornadofx.combobox
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.onChange
import tornadofx.splitpane
import tornadofx.style
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.vbox
import tornadofx.vgrow

/**
 * @author Kevin Ludwig
 */
class View : View("Transfer") {
    private val _config by di<Config>()

    override val root = vbox {
        JMetro(this, Style.DARK)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        splitpane {
            vgrow = Priority.ALWAYS

            @Suppress("UPPER_BOUND_VIOLATED_WARNING") add(Pane<Entry<*>>())
            @Suppress("UPPER_BOUND_VIOLATED_WARNING") add(Pane<Entry<*>>())
        }
    }

    inner class Pane<T : Entry<T>> : VBox() {
        private val source: Property<Source<T>> = SimpleObjectProperty<Source<T>>().apply { onChange { it?.let { cd(it.home) } } }
        private lateinit var _path: String
        private val name = SimpleStringProperty()
        private val tree = Tree<T>()

        init {
            hgrow = Priority.ALWAYS

            hbox {
                combobox(source) {
                    @Suppress("UNCHECKED_CAST")
                    items = _config.sources.toObservable() as ObservableList<Source<T>>
                }
                add(BreadCrumbBar<String>().apply {
                    tree.rootProperty().onChange {
                        it?.let {
                            var item: TreeItem<String>? = null
                            normalizePath(it.value.toString()).split('/').forEach { item = TreeItem(it).also { item?.children?.add(it) } }
                            selectedCrumb = item
                        }
                    }
                    selectedCrumbProperty().onChange {
                        val path = StringBuilder()
                        var item = it
                        while (item != null) {
                            path.insert(0, "${item!!.value}/")
                            item = item!!.parent
                        }
                        cd(path.toString())
                    }
                })
                textfield(name) {
                    hgrow = Priority.ALWAYS

                    style(append = true) { prefHeight = Dimension(27.0, Dimension.LinearUnits.px) }

                    addEventFilter(KeyEvent.KEY_PRESSED) { if (it.code == KeyCode.ENTER) text?.let { if (it.startsWith('/')) cd(it) else cd(tree.root.value.toString() + "/$it") } }
                }
            }
            add(tree)
        }

        private fun cd(path: String) {
            val normalizedPath = normalizePath(path)

            if (::_path.isInitialized && normalizedPath == _path) return
            _path = normalizedPath

            source.value?.let { source ->
                if (source.isValid(normalizedPath)) {
                    name.value = null
                    tree.root = source[normalizedPath].item
                    tree.populate(tree.root)
                }
            }
        }
    }

    companion object {
        private fun normalizePath(path: String): String {
            val normalizedPath = path.replace('\\', '/').replace("//", "/").replace("//", "/")
            return if (normalizedPath.length <= 1) normalizedPath else normalizedPath.removeSuffix("/")
        }
    }
}
