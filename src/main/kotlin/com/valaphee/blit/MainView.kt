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

package com.valaphee.blit

import com.valaphee.blit.config.Config
import com.valaphee.blit.config.ConfigView
import com.valaphee.blit.config.ConfigViewGeneral
import com.valaphee.blit.config.ConfigViewNetwork
import com.valaphee.blit.config.ConfigViewSources
import com.valaphee.blit.locale.Locale
import com.valaphee.blit.source.Entry
import com.valaphee.blit.source.Source
import com.valaphee.blit.source.SourceConfig
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumnBase
import javafx.scene.control.TextField
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.DataFormat
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import jfxtras.styles.jmetro.JMetroStyleClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import tornadofx.menu
import tornadofx.menubar
import tornadofx.onChange
import tornadofx.paddingTop
import tornadofx.populateTree
import tornadofx.progressbar
import tornadofx.separator
import tornadofx.setContent
import tornadofx.splitpane
import tornadofx.style
import tornadofx.useMaxSize
import tornadofx.vbox
import tornadofx.vgrow
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.UUID

/**
 * @author Kevin Ludwig
 */
class MainView : View("Blit"), CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    private val locale by di<Locale>()
    private val iconManifest by di<IconManifest>()
    private val _config by di<Config>()

    private val activity by di<Activity>()

    private var dragEntries = HashMap<UUID, List<Entry<*>>>()

    override val root = vbox {
        _config.theme.apply(this)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        setPrefSize(1000.0, 800.0)

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
                item(locale["main.menu.file.exit.name"]) { action { close() } }
            }
            menu(locale["main.menu.help.name"]) { item(locale["main.menu.help.about.name"]) { action { find<AboutView>().openModal(resizable = false) } } }
        }
        splitpane {
            vgrow = Priority.ALWAYS

            @Suppress("UPPER_BOUND_VIOLATED_WARNING") add(Pane<Entry<*>>())
            @Suppress("UPPER_BOUND_VIOLATED_WARNING") add(Pane<Entry<*>>())
        }
        hbox {
            progressbar(activity.progress) {
                hgrow = Priority.ALWAYS
                useMaxSize = true
            }
            button("...") {
                action {
                    find<ActivityView>().apply {
                        openWindow()
                        modalStage?.toFront()
                    }
                }
            }
        }
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
                                val path = it.value.path
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
                        @Suppress("NON_EXHAUSTIVE_WHEN_STATEMENT")
                        when (it.code) {
                            KeyCode.ENTER -> {
                                text?.let(::navigateRelative)
                                it.consume()
                            }
                            KeyCode.BACK_SPACE -> {
                                if (text?.isEmpty() != false) navigateRelative("..")
                                it.consume()
                            }
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
                launch {
                    activity.run(locale["main.navigator.task.navigate.name", canonicalPath]) {
                        val item = it.get(canonicalPath).item
                        _path = canonicalPath
                        launch(Dispatchers.Main) {
                            name.value = null
                            tree.root = item
                            tree.populate(tree.root)
                        }
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
                isEditable = true
                placeholder = Label("")

                column(locale["main.tree.column.name.title"], Entry<T>::self) {
                    tableColumnBaseSetWidth(this, 250.0)
                    setCellFactory {
                        object : TextFieldTreeTableCell<Entry<T>, Entry<T>>() {
                            override fun updateItem(item: Entry<T>?, empty: Boolean) {
                                super.updateItem(item, empty)

                                if (item == null || empty) {
                                    text = null
                                    graphic = null
                                } else {
                                    val name = item.name
                                    text = name
                                    graphic = if (item.directory) ImageView((iconManifest.folderIcons.firstOrNull { it.folderNames.contains(name) } ?: iconManifest.defaultFolderIcon).image) else run {
                                        val extension = name.substringAfterLast('.', "")
                                        ImageView((iconManifest.fileIcons.firstOrNull { it.fileExtensions.contains(extension) || it.fileNames.contains(name) } ?: iconManifest.defaultFileIcon).image)
                                    }
                                }
                            }

                            override fun startEdit() {
                                val textFieldNull = textFieldTreeTableCellTextField[this] == null

                                super.startEdit()

                                if (textFieldNull) {
                                    val textField = (textFieldTreeTableCellTextField[this] as TextField)
                                    textField.setOnAction {
                                        launch { activity.run(locale["main.tree.task.rename.name", item, textField.text]) { item.rename("${item.path.substringBeforeLast('/', "")}/${textField.text}") } }

                                        cancelEdit()
                                        it.consume()
                                    }
                                }
                            }

                            override fun cancelEdit() {
                                super.cancelEdit()

                                if (item == null || isEmpty) {
                                    text = null
                                    graphic = null
                                } else {
                                    val name = item.name
                                    text = name
                                    graphic = if (item.directory) ImageView((iconManifest.folderIcons.firstOrNull { it.folderNames.contains(name) } ?: iconManifest.defaultFolderIcon).image) else run {
                                        val extension = name.substringAfterLast('.', "")
                                        ImageView((iconManifest.fileIcons.firstOrNull { it.fileExtensions.contains(extension) || it.fileNames.contains(name) } ?: iconManifest.defaultFileIcon).image)
                                    }
                                }
                            }
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
                    cellFormat { text = if (it != 0L) locale.dateTimeFormat.format(it) else "" }
                }

                setRowFactory {
                    TreeTableRow<Entry<T>>().apply {
                        setOnMouseClicked {
                            if (isEmpty) selectionModel.clearSelection()
                            it.consume()
                        }
                        setOnDragDetected {
                            if (source != null) startDragAndDrop(TransferMode.COPY).apply {
                                dragView = snapshot(null, null)

                                setContent {
                                    val id = UUID.randomUUID()
                                    this[dragEntriesFormat] = id
                                    dragEntries[id] = selectionModel.selectedItems.map { it.value }
                                }
                            }
                            it.consume()
                        }
                        setOnDragOver {
                            if (source != null && (it.gestureSource != this && it.dragboard.hasFiles() || it.dragboard.hasContent(dragEntriesFormat))) it.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
                            it.consume()
                        }
                        setOnDragDropped {
                            source?.let { source ->
                                if (it.dragboard.hasContent(dragEntriesFormat)) {
                                    dragEntries.remove(it.dragboard.getContent(dragEntriesFormat))?.let { dragEntries ->
                                        val target = treeItem?.value ?: runBlocking { source.get(source.home) }
                                        if (!target.directory) TODO()

                                        fun transfer(entry: Entry<*>, path: String? = null) {
                                            if (entry.directory) launch { activity.run(locale["main.tree.task.populate.name", entry]) { entry.list().forEach { transfer(it, "${path?.let { "$path/" } ?: ""}${entry.name}") } } } else {
                                                val outStream = PipedOutputStream()
                                                val inStream = PipedInputStream(outStream)
                                                launch {
                                                    activity.run(locale["main.tree.task.download.name", entry]) {
                                                        outStream.use {
                                                            entry.transferTo(outStream)
                                                            outStream.flush()
                                                        }
                                                    }
                                                }
                                                launch { activity.run(locale["main.tree.task.upload.name", entry.name]) { inStream.use { target.transferFrom("${path?.let { "$path/" } ?: ""}${entry.name}", inStream, entry.size) } } }
                                            }
                                        }

                                        dragEntries.forEach(::transfer)

                                        it.isDropCompleted = true
                                    }
                                } else TODO()
                            }
                            it.consume()
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
                            if (it.list.size == 1 && it.list[0].value.directory) {
                                item(locale["main.tree.menu.open.name"]) { action { it.list.firstOrNull { it.value.directory }?.value?.let { navigateRelative(it.path) } ?: it.list.forEach(::open) } }
                                separator()
                                item(locale["main.tree.menu.new_directory.name"]) { action {} }
                                item(locale["main.tree.menu.new_file.name"]) { action {} }
                                separator()
                            } else if (it.list.none { it.value.directory }) {
                                item(locale["main.tree.menu.open.name"]) { action { it.list.forEach(::open) } }
                                separator()
                            }
                            item(locale["main.tree.menu.rename.name"]) { action {} }
                            item(locale["main.tree.menu.delete.name"]) { action { it.list.forEach(::delete) } }
                        }
                    }
                }

                setOnKeyPressed {
                    when (it.code) {
                        KeyCode.ENTER -> {
                            selectionModel.selectedItems.firstOrNull { it.value.directory }?.value?.let { navigateRelative(it.path) } ?: selectionModel.selectedItems.forEach(::open)
                            it.consume()
                        }
                        KeyCode.BACK_SPACE -> {
                            navigateRelative("..")
                            it.consume()
                        }
                        KeyCode.C -> if (it.isControlDown) {
                            Clipboard.getSystemClipboard().setContent {
                                launch(Dispatchers.Main) {
                                    activity.run(locale["main.tree.task.download.name"]) {
                                        suspend fun download(entry: Entry<T>, path: String? = null): List<File> = if (entry.directory) {
                                            File(_config.temporaryPath, entry.name).mkdir()
                                            entry.list().flatMap { download(it, "${path?.let { "$path/" } ?: ""}${entry.name}") }
                                        } else listOf(File(_config.temporaryPath, "${path?.let { "$path/" } ?: ""}${entry.name}").apply { FileOutputStream(this).use { entry.transferTo(it) } })

                                        putFiles(selectionModel.selectedItems.flatMap { download(it.value) })
                                    }
                                }
                            }
                            it.consume()
                        }
                        KeyCode.D -> if (it.isControlDown) {
                            selectionModel.selectedItems.forEach(::delete)
                            it.consume()
                        }
                        KeyCode.V -> if (it.isControlDown) {
                            with(Clipboard.getSystemClipboard()) {
                                if (hasFiles()) {
                                    val item = selectionModel.selectedItem ?: root
                                    val entry = item.value
                                    if (!entry.directory) TODO()
                                    files.forEach { file -> launch { activity.run(locale["main.tree.task.upload.name", file.name]) { FileInputStream(file).use { entry.transferFrom(file.name, it, file.length()) } } } }
                                }
                            }
                            it.consume()
                        }
                        KeyCode.DELETE -> {
                            selectionModel.selectedItems.forEach(::delete)
                            it.consume()
                        }
                        KeyCode.F5 -> {
                            populate(root)
                            it.consume()
                        }
                        else -> Unit
                    }
                }
                setOnMousePressed {
                    if (it.isPrimaryButtonDown && it.clickCount == 2) {
                        selectionModel.selectedItem?.let { if (!it.value.directory) open(it) }
                        it.consume()
                    }
                }
            }

            private fun open(item: TreeItem<Entry<T>>) {
                if (Desktop.isDesktopSupported()) {
                    val entry = item.value
                    launch {
                        activity.run(locale["main.tree.task.download.name", entry]) {
                            val file = File(_config.temporaryPath, entry.name).apply { FileOutputStream(this).use { entry.transferTo(it) } }
                            try {
                                Desktop.getDesktop().open(file)
                            } catch (_: IOException) {
                            }
                        }
                    }
                }
            }

            private fun delete(item: TreeItem<Entry<T>>) {
                val entry = item.value
                launch { activity.run(locale["main.tree.task.delete.name", entry.name]) { entry.delete() } }
            }

            internal fun populate(item: TreeItem<Entry<T>>) {
                launch {
                    activity.run(locale["main.tree.task.populate.name", item.value]) {
                        val children = item.value!!.list()
                        launch(Dispatchers.Main) {
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
    }

    companion object {
        private val windowsRootPath = "^[a-zA-Z]:[\\\\/].*\$".toRegex()
        internal val tableColumnBaseSetWidth = TableColumnBase::class.java.getDeclaredMethod("setWidth", Double::class.java).apply { isAccessible = true }
        private val textFieldTreeTableCellTextField = TextFieldTreeTableCell::class.java.getDeclaredField("textField").apply { isAccessible = true }
        private val dragEntriesFormat = DataFormat("application/blit-entry-id")

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
