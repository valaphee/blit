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

package com.valaphee.blit

import com.valaphee.blit.data.config.Config
import com.valaphee.blit.data.config.ConfigView
import com.valaphee.blit.data.config.ConfigViewGeneral
import com.valaphee.blit.data.config.ConfigViewNetwork
import com.valaphee.blit.data.config.ConfigViewSources
import com.valaphee.blit.data.locale.Locale
import com.valaphee.blit.data.manifest.IconManifest
import com.valaphee.blit.source.Entry
import com.valaphee.blit.source.Source
import com.valaphee.blit.source.SourceConfig
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumnBase
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetroStyleClass
import org.bridj.cpp.com.COMRuntime
import org.bridj.cpp.com.shell.ITaskbarList3
import org.controlsfx.control.BreadCrumbBar
import org.controlsfx.control.textfield.CustomTextField
import tornadofx.Dimension
import tornadofx.View
import tornadofx.action
import tornadofx.bind
import tornadofx.button
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.combobox
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.item
import tornadofx.label
import tornadofx.menu
import tornadofx.menubar
import tornadofx.onChange
import tornadofx.paddingTop
import tornadofx.populateTree
import tornadofx.progressbar
import tornadofx.runLater
import tornadofx.separator
import tornadofx.setContent
import tornadofx.splitpane
import tornadofx.style
import tornadofx.vbox
import tornadofx.vgrow
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.util.concurrent.CompletableFuture

/**
 * @author Kevin Ludwig
 */
class MainView : View("Blit") {
    private val locale by di<Locale>()
    private val iconManifest by di<IconManifest>()
    private val _config by di<Config>()

    private val activity = MainActivity().apply {
        val version = System.getProperty("os.version").toFloatOrNull()
        if (System.getProperty("os.name").startsWith("Windows") && version != null && version >= 6.1f) {
            val iTaskbarList3 = CompletableFuture.supplyAsync({ COMRuntime.newInstance(ITaskbarList3::class.java) }, comExecutor).join()
            val hWnd by lazy { primaryStage.hWnd }
            progress.onChange {
                val _hWnd = hWnd
                comExecutor.execute {
                    iTaskbarList3.SetProgressState(_hWnd, ITaskbarList3.TbpFlag.TBPF_NORMAL)
                    iTaskbarList3.SetProgressValue(_hWnd, (it * 100).toLong(), 100)
                }
            }
        }
    }
    private val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale.toJavaLocale())

    override val root = vbox {
        _config.theme.apply(this)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        prefWidth = 1000.0
        prefHeight = 800.0

        menubar {
            menu(locale["main.menu.file.name"]) {
                item(locale["main.menu.file.general.name"]) {
                    action {
                        find<ConfigView> {
                            select<ConfigViewGeneral>()
                            openModal()
                        }
                    }
                }
                item(locale["main.menu.file.sources.name"]) {
                    action {
                        find<ConfigView> {
                            select<ConfigViewSources>()
                            openModal()
                        }
                    }
                }
                item(locale["main.menu.file.network.name"]) {
                    action {
                        find<ConfigView> {
                            select<ConfigViewNetwork>()
                            openModal()
                        }
                    }
                }
                separator()
                item(locale["main.menu.file.exit.name"]) { action { (scene.window as Stage).close() } }
            }
            menu(locale["main.menu.help.name"]) { item(locale["main.menu.help.about.name"]) { action { find<AboutView>().openModal(resizable = false) } } }
        }
        splitpane {
            vgrow = Priority.ALWAYS

            @Suppress("UPPER_BOUND_VIOLATED_WARNING") add(Pane<Entry<*>>())
            @Suppress("UPPER_BOUND_VIOLATED_WARNING") add(Pane<Entry<*>>())
        }
        label(activity.name)
        progressbar(activity.progress)
    }

    inner class Pane<T : Entry<T>> : VBox() {
        private val sourceConfig = SimpleObjectProperty<SourceConfig>().apply {
            onChange {
                it?.let {
                    source?.close()

                    @Suppress("UNCHECKED_CAST")
                    source = (it.newSource() as Source<T>)

                    _path = null
                    tree.root = null
                    navigate(source!!.home)
                }
            }
        }
        private var source: Source<T>? = null
        private var _path: String? = null
        private val name = SimpleStringProperty()
        private val tree = Tree<T>()

        init {
            hgrow = Priority.ALWAYS

            hbox {
                combobox(sourceConfig, _config.sources)
                add(CustomTextField().apply {
                    bind(name)

                    hgrow = Priority.ALWAYS
                    style(true) { prefHeight = Dimension(27.0, Dimension.LinearUnits.px) }

                    left = BreadCrumbBar<String>().apply {
                        style(true) { paddingTop = 2.0 }

                        tree.rootProperty().onChange {
                            it?.let {
                                val path = it.value.toString()
                                selectedCrumb = if (path == "/") BreadCrumbBar.buildTreeModel("/") else BreadCrumbBar.buildTreeModel(*path.toCanonicalPath().toTypedArray().apply { if (this.getOrNull(0)?.isEmpty() == true) this[0] = "/" })
                            }
                        }
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

                    addEventFilter(KeyEvent.KEY_PRESSED) {
                        when (it.code) {
                            KeyCode.ENTER -> text?.let(::navigateRelative)
                            KeyCode.BACK_SPACE -> if (text?.isEmpty() != false) navigateRelative("..")
                        }
                    }
                })
                button(locale["main.navigator.go.text"]) { action { name.value?.let(::navigateRelative) } }
            }
            add(tree)
        }

        private fun navigate(path: String) {
            var canonicalPath = path.toCanonicalPath().joinToString("/")
            if (!canonicalPath.endsWith('/')) canonicalPath = "$canonicalPath/"

            if (canonicalPath == _path) return

            source?.let {
                activity.launch(locale["main.navigator.task.navigate.name", canonicalPath]) {
                    val item = it.get(canonicalPath).item
                    _path = canonicalPath
                    runLater {
                        name.value = null
                        tree.root = item
                        tree.populate(tree.root)
                    }
                }
            }
        }

        fun navigateRelative(path: String) = navigate(if (path.startsWith('/') || path.matches(windowsRootPath)) path else "${tree.root.value}/$path")

        inner class Tree<T : Entry<T>> : TreeTableView<Entry<T>>() {
            init {
                vgrow = Priority.ALWAYS
                isShowRoot = false
                selectionModel.selectionMode = SelectionMode.MULTIPLE

                placeholder = Label("")

                column(locale["main.tree.column.name.title"], Entry<T>::self) {
                    tableColumnBaseSetWidth(this, 250.0)
                    cellFormat {
                        val name = it.name
                        text = name
                        graphic = if (it.directory) ImageView((iconManifest.folderIcons.firstOrNull { it.folderNames.contains(name) } ?: iconManifest.defaultFolderIcon).image) else {
                            val extension = name.substringAfterLast('.', "")
                            ImageView((iconManifest.fileIcons.firstOrNull { it.fileExtensions.contains(extension) || it.fileNames.contains(name) } ?: iconManifest.defaultFileIcon).image)
                        }
                    }
                    setComparator { a, b -> a.name.compareTo(b.name) }
                }
                column(locale["main.tree.column.size.title"], Entry<T>::self) {
                    tableColumnBaseSetWidth(this, 75.0)
                    cellFormat { text = if (it.directory) "" else _config.dataSizeUnit.format(it.size) }
                    setComparator { a, b -> a.size.compareTo(b.size) }
                }
                column(locale["main.tree.column.modified.title"], Entry<T>::modifyTime) {
                    tableColumnBaseSetWidth(this, 125.0)
                    cellFormat { text = if (it != 0L) dateFormat.format(it) else "" }
                }

                setRowFactory {
                    object : TreeTableRow<Entry<T>>() {
                        init {
                            setOnDragDetected {
                                startDragAndDrop(TransferMode.MOVE).apply {
                                    setContent {
                                        activity.runBlocking(locale["main.tree.task.download.name"]) {
                                            suspend fun flatten(entry: Entry<T>, path: String? = null): List<File> = if (entry.directory) {
                                                File(_config.temporaryPath, entry.name).mkdir()
                                                entry.list().flatMap { flatten(it, "${path?.let { "$path/" } ?: ""}${entry.name}") }
                                            } else listOf(File(_config.temporaryPath, "${path?.let { "$path/" } ?: ""}${entry.name}").apply { FileOutputStream(this).use { entry.transferTo(it) } })

                                            putFiles(selectionModel.selectedItems.flatMap { flatten(it.value) })
                                        }
                                    }
                                }

                                it.consume()
                            }
                            setOnDragOver {
                                if (it.gestureSource != this && it.dragboard.hasFiles()) it.acceptTransferModes(*TransferMode.COPY_OR_MOVE)

                                it.consume()
                            }
                            setOnDragDropped {
                                it.isDropCompleted = true

                                it.consume()
                            }
                        }
                    }
                }

                selectionModel.selectedItems.onChange {
                    contextMenu = ContextMenu().apply {
                        if (it.list.isEmpty()) {
                            item(locale["main.tree.menu.parent.name"]) { action { navigateRelative("..") } }
                            separator()
                            item(locale["main.tree.menu.new_directory.name"]) { action {} }
                            item(locale["main.tree.menu.new_file.name"]) { action {} }
                            separator()
                            item(locale["main.tree.menu.refresh.name"]) { action { populate(root) } }
                        } else {
                            item(locale["main.tree.menu.open.name"]) { action { it.list.firstOrNull { it.value.directory }?.value?.let { navigateRelative(it.toString()) } ?: it.list.forEach(::open) } }
                            separator()
                            item(locale["main.tree.menu.rename.name"]) { action { it.list.forEach(::rename) } }
                            item(locale["main.tree.menu.delete.name"]) { action { it.list.forEach(::delete) } }
                        }
                    }
                }

                setOnKeyPressed {
                    when (it.code) {
                        KeyCode.ENTER -> selectionModel.selectedItems.firstOrNull { it.value.directory }?.value?.let { navigateRelative(it.toString()) } ?: selectionModel.selectedItems.forEach(::open)
                        KeyCode.BACK_SPACE -> navigateRelative("..")
                        KeyCode.C -> if (it.isControlDown) Clipboard.getSystemClipboard().setContent {
                            activity.runBlocking(locale["main.tree.task.download.name"]) {
                                suspend fun flatten(entry: Entry<T>, path: String? = null): List<File> = if (entry.directory) {
                                    File(_config.temporaryPath, entry.name).mkdir()
                                    entry.list().flatMap { flatten(it, "${path?.let { "$path/" } ?: ""}${entry.name}") }
                                } else listOf(File(_config.temporaryPath, "${path?.let { "$path/" } ?: ""}${entry.name}").apply { FileOutputStream(this).use { entry.transferTo(it) } })

                                putFiles(selectionModel.selectedItems.flatMap { flatten(it.value) })

                                it.consume()
                            }
                        }
                        KeyCode.D -> if (it.isControlDown) selectionModel.selectedItems.forEach(::delete)
                        KeyCode.V -> if (it.isControlDown) with(Clipboard.getSystemClipboard()) {
                            if (hasFiles()) {
                                val item = selectionModel.selectedItem ?: root
                                val entry = item.value
                                if (!entry.directory) TODO()
                                files.forEach { file -> activity.launch(locale["main.tree.task.upload.name", file.name]) { FileInputStream(file).use { entry.transferFrom(file.name, it, file.length()) } } }
                            }
                        }
                        KeyCode.DELETE -> selectionModel.selectedItems.forEach(::delete)
                        KeyCode.F2 -> selectionModel.selectedItems.forEach(::rename)
                        KeyCode.F5 -> populate(root)
                    }
                }
                setOnMousePressed { if (it.isPrimaryButtonDown && it.clickCount == 2) selectionModel.selectedItem?.let { if (!it.value.directory) open(it) } }
            }

            private fun open(item: TreeItem<Entry<T>>) {
                if (Desktop.isDesktopSupported()) {
                    val entry = item.value
                    activity.launch(locale["main.tree.task.download.name", entry]) {
                        val file = File(_config.temporaryPath, entry.name).apply { FileOutputStream(this).use { entry.transferTo(it) } }
                        try {
                            Desktop.getDesktop().open(file)
                        } catch (_: IOException) {}
                    }
                }
            }

            private fun rename(item: TreeItem<Entry<T>>) {
                val entry = item.value
                RenameView(entry.name) { activity.launch(locale["main.tree.task.rename.name", entry, it]) { entry.rename(it) } }.openModal(resizable = false)
            }

            private fun delete(item: TreeItem<Entry<T>>) {
                val entry = item.value
                activity.launch(locale["main.tree.task.delete.name", entry]) { entry.delete() }
            }

            internal fun populate(item: TreeItem<Entry<T>>) {
                activity.launch(locale["main.tree.task.populate.name", item.value]) {
                    val children = item.value!!.list()
                    runLater {
                        populateTree(item, { entry ->
                            if (entry.directory) object : TreeItem<Entry<T>>(entry) {
                                init {
                                    expandedProperty().onChange { if (it) populate(this) }
                                }

                                override fun isLeaf() = false
                            } else TreeItem(entry)
                        }) { if (it.isExpanded) children else emptyList() }
                    }
                }
            }
        }
    }

    companion object {
        private val windowsRootPath = "^[a-zA-Z]:[\\\\/].*\$".toRegex()
        private val tableColumnBaseSetWidth = TableColumnBase::class.java.getDeclaredMethod("setWidth", Double::class.java).apply { isAccessible = true }

        internal fun String.toCanonicalPath(): List<String> {
            replace('\\', '/').apply {
                val canonicalPath = mutableListOf<String>()
                split('/').forEach {
                    when (it) {
                        "." -> Unit
                        ".." -> canonicalPath.lastOrNull()?.let { if (it != "..") canonicalPath.removeLast() else canonicalPath.add(it) }
                        else -> if (it.isNotEmpty()) canonicalPath.add(it)
                    }
                }
                return canonicalPath.filter(String::isNotEmpty).let { if (startsWith("/")) it.toMutableList().apply { add(0, "") } else it }
            }
        }
    }
}
