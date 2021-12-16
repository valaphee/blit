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

package com.valaphee.blit.app

import com.valaphee.blit.Entry
import com.valaphee.blit.Source
import com.valaphee.blit.app.config.Config
import com.valaphee.blit.app.config.ConfigView
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.controlsfx.control.BreadCrumbBar
import org.controlsfx.control.textfield.CustomTextField
import tornadofx.Dimension
import tornadofx.View
import tornadofx.action
import tornadofx.bind
import tornadofx.button
import tornadofx.combobox
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.item
import tornadofx.menu
import tornadofx.menubar
import tornadofx.onChange
import tornadofx.paddingTop
import tornadofx.separator
import tornadofx.splitpane
import tornadofx.style
import tornadofx.vbox
import tornadofx.vgrow

/**
 * @author Kevin Ludwig
 */
class MainView : View("Blit") {
    private val iconManifest by di<IconManifest>()
    private val _config by di<Config>()
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val root = vbox {
        JMetro(this, Style.DARK)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        prefWidth = 1000.0
        prefHeight = 800.0

        menubar {
            menu("File") {
                item("Sources") { action { find<ConfigView>().openModal() } }
                separator()
                item("Exit") { action { (scene.window as Stage).close() } }
            }
            menu("Help") { item("About") { action { find<AboutView>().openModal(resizable = false) } } }
        }

        splitpane {
            vgrow = Priority.ALWAYS

            @Suppress("UPPER_BOUND_VIOLATED_WARNING") add(Pane<Entry<*>>())
            @Suppress("UPPER_BOUND_VIOLATED_WARNING") add(Pane<Entry<*>>())
        }
    }

    inner class Pane<T : Entry<T>> : VBox(), Navigator {
        private val source = SimpleObjectProperty<Source<T>>().apply { onChange { it?.let { navigate(it.home) } } }
        private lateinit var _path: String
        private val name = SimpleStringProperty()
        private val tree = Tree<T>(iconManifest, ioScope, this)

        init {
            hgrow = Priority.ALWAYS

            hbox {
                combobox(source) {
                    @Suppress("UNCHECKED_CAST")
                    items = _config.sources as ObservableList<Source<T>>
                }
                add(CustomTextField().apply {
                    bind(name)

                    hgrow = Priority.ALWAYS
                    style(true) { prefHeight = Dimension(27.0, Dimension.LinearUnits.px) }

                    left = BreadCrumbBar<String>().apply {
                        style(true) { paddingTop = 2.0 }

                        tree.rootProperty().onChange { it?.let { selectedCrumb = BreadCrumbBar.buildTreeModel(*normalizePath(it.value.toString()).split('/').toTypedArray()) } }
                        selectedCrumbProperty().onChange {
                            val path = StringBuilder()
                            var item = it
                            while (item != null) {
                                path.insert(0, "${item.value}/")
                                item = item.parent
                            }
                            navigate(path.toString())
                        }
                    }

                    addEventFilter(KeyEvent.KEY_PRESSED) { if (it.code == KeyCode.ENTER) text?.let(::navigateRelative) }
                })
                button("Go") { action { name.value?.let(::navigateRelative) } }
            }
            add(tree)
        }

        override fun navigate(path: String) {
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

        override fun navigateRelative(path: String) = navigate(if (path.startsWith('/')) path else tree.root.value.toString() + "/$path")
    }

    companion object {
        internal fun normalizePath(path: String): String {
            val normalizedPath = path.replace('\\', '/').replace("//", "/").replace("//", "/")
            return if (normalizedPath.length <= 1) normalizedPath else normalizedPath.removeSuffix("/")
        }
    }
}
