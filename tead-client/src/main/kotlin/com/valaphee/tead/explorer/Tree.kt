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
