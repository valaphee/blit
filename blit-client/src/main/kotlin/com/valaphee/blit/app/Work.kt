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

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tornadofx.onChange
import tornadofx.runLater
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * @author Kevin Ludwig
 */
class Work {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val tasks = mutableListOf<Task>()

    val name: StringProperty = SimpleStringProperty("Idle")
    val progress: DoubleProperty = SimpleDoubleProperty(0.0)

    fun <T> runBlocking(name: String, block: suspend () -> T): T {
        this.name.value = name
        return runBlocking { run(name, block) }
    }

    fun launch(name: String, block: suspend () -> Unit): Job {
        this.name.value = name
        return coroutineScope.launch { run(name, block) }
    }

    private suspend fun <T> run(name: String, block: suspend () -> T): T {
        val task = Task(name, SimpleDoubleProperty(0.0).apply { onChange { runLater { progress.value = tasks.map { it.progress.value }.average() } } })
        tasks += task
        val value = withContext(task) { block() }
        tasks -= task
        runLater {
            progress.value = tasks.map { it.progress.value }.average()
            this.name.value = tasks.lastOrNull()?.name ?: "Idle"
        }
        return value
    }

    class Task(
        val name: String,
        val progress: DoubleProperty
    ) : AbstractCoroutineContextElement(Task) {
        companion object Key : CoroutineContext.Key<Task>
    }
}

var CoroutineContext.progress: Double
    get() = get(Work.Task)!!.progress.value
    set(value) { get(Work.Task)!!.progress.value = value }
