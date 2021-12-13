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

package com.valaphee.tead.explorer

import javafx.scene.control.TreeItem
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.name

/**
 * @author Kevin Ludwig
 */
class LocalEntry(
    private val path: File
) : Entry<LocalEntry> {
    override val item: TreeItem<Entry<LocalEntry>> = TreeItem(this)

    override val name: String get() = path.name
    override val size get() = if (path.isDirectory) children.size.toLong() else path.length()
    override val children get() = path.listFiles()?.map { LocalEntry(it) } ?: emptyList()

    private val watchKey = if (path.isDirectory) path.toPath().register(watcherService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE) else null

    override fun update() {
        if (!item.isExpanded) return

        watchKey?.pollEvents()?.forEach {
            when (it.kind()) {
                StandardWatchEventKinds.ENTRY_CREATE -> item.children.add(LocalEntry(path.toPath().resolve((it.context() as Path)).toFile()).item)
                StandardWatchEventKinds.ENTRY_DELETE -> {
                    val name = (it.context() as Path).name
                    val iterator = item.children.iterator()
                    while (iterator.hasNext()) {
                        val child = iterator.next().value
                        if (child.name == name) {
                            iterator.remove()
                            break
                        }
                    }
                }
            }
        }

        children.forEach { it.update() }
    }

    companion object {
        private val watcherService = FileSystems.getDefault().newWatchService()
    }
}
