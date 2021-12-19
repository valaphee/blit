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

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.valaphee.blit.source.NotFoundException
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tornadofx.onChange
import tornadofx.runLater
import tornadofx.warning
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Executors
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * @author Kevin Ludwig
 */
class Worker {
    private val coroutineScope = CoroutineScope(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), ThreadFactoryBuilder().setNameFormat("blit-%d").setDaemon(true).build()).asCoroutineDispatcher() + SupervisorJob())
    private val tasks = ConcurrentLinkedDeque<Task>()

    val name: StringProperty = SimpleStringProperty("")
    val progress: DoubleProperty = SimpleDoubleProperty(0.0)

    fun runBlocking(name: String, block: suspend () -> Unit) = runBlocking { run(name, block) }

    fun launch(name: String, block: suspend () -> Unit) = coroutineScope.launch { run(name, block) }

    private suspend fun run(name: String, block: suspend () -> Unit) {
        val task = Task(name, SimpleDoubleProperty().apply { onChange { runLater { progress.value = tasks.map { it.progress.value }.average() } } })
        tasks += task
        task.progress.value = 0.0
        runLater { this.name.value = tasks.joinToString(" | ") { it.name } }
        withContext(task) {
            try {
                block()
            } catch (ex: NotFoundException) {
                runLater { warning("Not found", ex.path, title = name) }
            }
        }
        tasks -= task
        runLater {
            progress.value = tasks.map { it.progress.value }.average()
            this.name.value = tasks.joinToString(" | ") { it.name }
        }
    }

    class Task(
        val name: String,
        val progress: DoubleProperty
    ) : AbstractCoroutineContextElement(Task) {
        companion object Key : CoroutineContext.Key<Task>
    }
}

var CoroutineContext.progress: Double
    get() = get(Worker.Task)!!.progress.value
    set(value) { get(Worker.Task)!!.progress.value = value }
