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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableRow
import javafx.scene.control.TreeTableView
import javafx.scene.image.ImageView
import javafx.scene.input.TransferMode
import javafx.scene.layout.Priority
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.onChange
import tornadofx.populateTree
import tornadofx.setContent
import tornadofx.vgrow
import java.io.File
import java.io.FileOutputStream
import java.text.StringCharacterIterator
import kotlin.math.abs

/**
 * @author Kevin Ludwig
 */
class Tree<T : Entry<T>> : TreeTableView<Entry<T>>() {
    init {
        vgrow = Priority.ALWAYS
        isShowRoot = false
        selectionModel.selectionMode = SelectionMode.MULTIPLE

        column("Name", Entry<T>::self) {
            cellFormat {
                val name = it.name
                text = name
                graphic = if (it.directory) ImageView((manifest.folderIcons.firstOrNull { it.folderNames.contains(name) } ?: manifest.defaultFolderIcon).image) else {
                    val extension = name.substringAfterLast('.', "")
                    ImageView((manifest.fileIcons.firstOrNull { it.fileExtensions.contains(extension) || it.fileNames.contains(name) } ?: manifest.defaultFileIcon).image)
                }
            }
        }
        column("Size", Entry<T>::self) { cellFormat { text = if (it.directory) "${it.children.size}" else humanReadableSizeBinary(it.size) } }

        setRowFactory {
            object : TreeTableRow<Entry<T>>() {
                init {
                    setOnDragDetected {
                        startDragAndDrop(TransferMode.MOVE).apply { setContent { putFiles(selectionModel.selectedItems.mapNotNull { if (it.value.directory) null else File(tmpdir, it.value.name) }) } }
                        it.consume()
                    }
                    setOnDragDone {
                        selectionModel.selectedItems.zip(it.dragboard.files).forEach { (item, file) -> if (!item.value.directory) FileOutputStream(file).use { item.value.transferTo(it) } }
                        it.consume()
                    }
                    setOnDragOver {
                        if (it.dragboard.hasFiles()) it.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
                        it.consume()
                    }
                    setOnDragDropped {
                        if (it.dragboard.hasFiles()) it.isDropCompleted = true
                        it.consume()
                    }
                }
            }
        }
    }

    fun populate(item: TreeItem<Entry<T>>) {
        populateTree(item, { entry -> TreeItem(entry).apply { expandedProperty().onChange { if (it) populate(this) } } }) { if (it.isExpanded || it.parent.isExpanded) it.value.children else emptyList() }
    }

    companion object {
        private val manifest = jacksonObjectMapper().readValue<Manifest>(Tree::class.java.getResourceAsStream("/transfer/.manifest")!!)
        private val tmpdir = System.getProperty("java.io.tmpdir")

        private fun humanReadableSizeSi(size: Long): String {
            var sizeVar = size
            if (-1000 < sizeVar && sizeVar < 1000) return "$sizeVar B"
            val suffix = StringCharacterIterator("kMGTPE")
            while (sizeVar <= -999950 || sizeVar >= 999950) {
                sizeVar /= 1000
                suffix.next()
            }
            return String.format("%.1f %cB", sizeVar / 1000.0, suffix.current())
        }

        private fun humanReadableSizeBinary(size: Long): String {
            val sizeAbs = if (size == Long.MIN_VALUE) Long.MAX_VALUE else abs(size)
            if (sizeAbs < 1024) return "$size B"
            var sizeVar = sizeAbs
            val suffix = StringCharacterIterator("KMGTPE")
            var i = 40
            while (i >= 0 && sizeAbs > 0xfffccccccccccccL shr i) {
                sizeVar = sizeVar shr 10
                suffix.next()
                i -= 10
            }
            sizeVar *= java.lang.Long.signum(size).toLong()
            return String.format("%.1f %ciB", sizeVar / 1024.0, suffix.current())
        }
    }
}
