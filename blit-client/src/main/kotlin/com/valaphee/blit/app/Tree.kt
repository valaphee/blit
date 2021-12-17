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
import com.valaphee.blit.app.config.Config
import javafx.scene.control.ContextMenu
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumnBase
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import tornadofx.action
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.item
import tornadofx.onChange
import tornadofx.populateTree
import tornadofx.runLater
import tornadofx.setContent
import tornadofx.vgrow
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.Locale

/**
 * @author Kevin Ludwig
 */
class Tree<T : Entry<T>>(
    private val iconManifest: IconManifest,
    private val _config: Config,
    private val work: Work,
    private val navigator: Navigator
) : TreeTableView<Entry<T>>() {
    init {
        vgrow = Priority.ALWAYS
        isShowRoot = false
        selectionModel.selectionMode = SelectionMode.MULTIPLE

        column("Name", Entry<T>::self) {
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
        column("Size", Entry<T>::self) {
            tableColumnBaseSetWidth(this, 75.0)
            cellFormat { text = if (it.directory) "" else _config.dataSizeUnit.format(it.size) }
            setComparator { a, b -> a.size.compareTo(b.size) }
        }
        column("Modified", Entry<T>::modifyTime) {
            tableColumnBaseSetWidth(this, 125.0)
            cellFormat { text = dateFormat.format(it) }
        }

        setRowFactory {
            object : TreeTableRow<Entry<T>>() {
                init {
                    setOnDragDetected {
                        startDragAndDrop(TransferMode.MOVE).apply {
                            setContent {
                                work.runBlocking("Downloading") {
                                    suspend fun flatten(entry: Entry<T>, path: String? = null): List<File> = if (entry.directory) {
                                        File(tmpdir, entry.name).mkdir()
                                        entry.list().flatMap { flatten(it, "${path?.let { "$path/" } ?: ""}${entry.name}") }
                                    } else listOf(File(tmpdir, "${path?.let { "$path/" } ?: ""}${entry.name}").apply { FileOutputStream(this).use { entry.transferTo(it) } })

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
        setOnKeyPressed {
            if (it.isControlDown) when (it.code) {
                KeyCode.C -> Clipboard.getSystemClipboard().setContent {
                    work.runBlocking("Downloading") {
                        suspend fun flatten(entry: Entry<T>, path: String? = null): List<File> = if (entry.directory) {
                            File(tmpdir, entry.name).mkdir()
                            entry.list().flatMap { flatten(it, "${path?.let { "$path/" } ?: ""}${entry.name}") }
                        } else listOf(File(tmpdir, "${path?.let { "$path/" } ?: ""}${entry.name}").apply { FileOutputStream(this).use { entry.transferTo(it) } })

                        putFiles(selectionModel.selectedItems.flatMap { flatten(it.value) })

                        it.consume()
                    }
                }
                KeyCode.V -> with(Clipboard.getSystemClipboard()) {
                    if (hasFiles()) {
                        val entry = selectionModel.selectedItem.value
                        if (!entry.directory) TODO()
                        files.forEach { file -> work.launch("Uploading ${file.name}") { FileInputStream(file).use { entry.transferFrom(file.name, it, file.length()) } } }
                    }
                }
            }
        }

        selectionModel.selectedItemProperty().onChange {
            it?.let {
                contextMenu = ContextMenu().apply {
                    val value = it.value
                    item("Open") { action { if (value.directory) navigator.navigateRelative(value.name) else if (Desktop.isDesktopSupported()) work.launch("Downloading $value") { Desktop.getDesktop().open(File(tmpdir, value.name).apply { FileOutputStream(this).use { value.transferTo(it) } }) } } }
                }
            }
        }
    }

    fun populate(item: TreeItem<Entry<T>>) {
        work.launch("Populating ${item.value}") {
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

    companion object {
        private val tableColumnBaseSetWidth = TableColumnBase::class.java.getDeclaredMethod("setWidth", Double::class.java).apply { isAccessible = true }
        private val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
        private val tmpdir = System.getProperty("java.io.tmpdir")
    }
}
