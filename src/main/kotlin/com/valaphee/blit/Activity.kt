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

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.inject.Singleton
import com.valaphee.blit.source.NotFoundException
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tornadofx.getValue
import tornadofx.runLater
import tornadofx.setValue
import tornadofx.toObservable
import java.util.concurrent.Executors
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * @author Kevin Ludwig
 */
@Singleton
class Activity {
    private val coroutineScope = CoroutineScope(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), ThreadFactoryBuilder().setNameFormat("blit-%d").setDaemon(true).build()).asCoroutineDispatcher() + SupervisorJob())

    val tasks = mutableListOf<Task>().toObservable()
    val progress: DoubleProperty = SimpleDoubleProperty(0.0)
    private var update = false

    fun runBlocking(name: String, block: suspend () -> Unit) = runBlocking { run(name, block) }

    fun launch(name: String, block: suspend () -> Unit) = coroutineScope.launch { run(name, block) }

    private suspend fun run(name: String, block: suspend () -> Unit) {
        val task = Task(name, System.currentTimeMillis())

        runLater {
            tasks += task

            run()
        }

        withContext(task) {
            try {
                block()
            } catch (ex: NotFoundException) {
                runLater { ErrorView("Not found", "${ex.path} not found").openModal(resizable = false) }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }

        runLater { tasks -= task }
    }

    private fun run() {
        if (update) return

        update = true
        coroutineScope.launch {
            while (tasks.isNotEmpty()) {
                runLater { progress.value = tasks.map { it.progress }.average() }
                delay(1000)
            }
        }
        progress.value = 0.0
        update = false
    }

    class Task(
        val name: String,
        val time: Long,
    ) : AbstractCoroutineContextElement(Task) {
        companion object Key : CoroutineContext.Key<Task>

        val progressProperty: DoubleProperty = SimpleDoubleProperty(0.0)
        var progress by progressProperty
    }
}

var CoroutineContext.progress: Double
    get() = get(Activity.Task)!!.progress
    set(value) {
        get(Activity.Task)!!.progress = value
    }
