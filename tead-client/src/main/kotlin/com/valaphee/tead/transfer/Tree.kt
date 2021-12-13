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
import com.valaphee.tead.util.humanReadableSizeBinary
import javafx.scene.control.ContextMenu
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tornadofx.cellFormat
import tornadofx.checkmenuitem
import tornadofx.column
import tornadofx.item
import tornadofx.onChange
import tornadofx.populateTree
import tornadofx.separator
import tornadofx.vgrow
import kotlin.coroutines.CoroutineContext

/**
 * @author Kevin Ludwig
 */
class Tree<T : Entry<T>>(
    override val coroutineContext: CoroutineContext
) : TreeTableView<Entry<T>>(), CoroutineScope {
    lateinit var job: Job

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
        column("Size", Entry<T>::self) {
            cellFormat {
                text = if (it.directory) "${it.children.size}" else humanReadableSizeBinary(it.size)
            }
        }

        selectionModel.selectedItemProperty().onChange {
            it?.let {
                contextMenu = ContextMenu().apply {
                    if (it.value.directory) {
                        item("Open")
                    } else {
                        item("Open")
                        item("Open with")
                    }
                    separator()
                    item("Rename")
                    item("Delete")
                    separator()
                    checkmenuitem("Sync")
                }
            }
        }
    }

    fun populate(item: TreeItem<Entry<T>>) {
        launch { populateTree(item, { entry -> TreeItem(entry).apply { expandedProperty().onChange { if (it) populate(this) } } }) { if (it.isExpanded || it.parent.isExpanded) it.value.children else emptyList() } }
    }

    fun startUpdates() {
        stopUpdates()
        job = launch {
            while (true) {
                update()
                delay(1000)
            }
        }
    }

    fun update() {
        root.value.update()
    }

    fun stopUpdates() {
        if (this::job.isInitialized) job.cancel()
    }

    companion object {
        private val manifest = jacksonObjectMapper().readValue<Manifest>(Tree::class.java.getResourceAsStream("/transfer/.manifest")!!)
    }
}
