/*
 * Copyright (c) 2021, Valaphee.
 * All rights reserved.
 */

package com.valaphee.tead.explorer

import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeTableView
import javafx.scene.layout.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tornadofx.column
import tornadofx.populate
import tornadofx.vgrow
import kotlin.coroutines.CoroutineContext

/**
 * @author Kevin Ludwig
 */
class Tree<T : Entry<T>>(
    private val entry: Entry<T>,
    override val coroutineContext: CoroutineContext
) : TreeTableView<Entry<T>>(entry.item), CoroutineScope {
    lateinit var job: Job

    init {
        vgrow = Priority.ALWAYS
        isShowRoot = false
        selectionModel.selectionMode = SelectionMode.MULTIPLE

        column("Name", Entry<T>::name)
        column("Size", Entry<T>::size)

        populate({ it.item }) { it.value.children }
    }

    fun startUpdates() {
        stopUpdates()
        job = launch {
            while (true) {
                entry.update()
                delay(1000)
            }
        }
    }

    fun stopUpdates() {
        if (this::job.isInitialized) job.cancel()
    }
}
