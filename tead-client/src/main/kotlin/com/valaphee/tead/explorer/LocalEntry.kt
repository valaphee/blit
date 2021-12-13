/*
 * Copyright (c) 2021, Valaphee.
 * All rights reserved.
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
