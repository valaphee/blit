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

import com.google.inject.Singleton
import com.valaphee.blit.source.GeneralError
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tornadofx.toObservable
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * @author Kevin Ludwig
 */
@Singleton
class Activity {
    val tasks = mutableListOf<Task>().toObservable()
    val progress: DoubleProperty = SimpleDoubleProperty(0.0)
    private var update = false

    suspend fun run(name: String, block: suspend () -> Unit) {
        val task = Task(name, System.currentTimeMillis())

        coroutineScope {
            launch(Dispatchers.Main) { tasks += task }

            run()

            try {
                withContext(task) { block() }
            } catch (error: GeneralError) {
                launch(Dispatchers.Main) { ErrorView(error.error, error.message!!).openModal(resizable = false) }
            } finally {
                launch(Dispatchers.Main) { tasks -= task }
            }
        }
    }

    private fun run() {
        if (update) return

        update = true
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            while (tasks.isNotEmpty()) {
                progress.value = tasks.onEach { if (it.progressProperty.value != it.progress) it.progressProperty.value = it.progress }.map { if (it.progress == -1.0) 0.0 else it.progress }.average()
                delay(50)
            }
            progress.value = 0.0
        }
        update = false
    }

    class Task(
        val name: String,
        val time: Long,
    ) : AbstractCoroutineContextElement(Task) {
        companion object Key : CoroutineContext.Key<Task>

        val progressProperty = SimpleDoubleProperty(-1.0)

        var progress = -1.0
    }
}

var CoroutineContext.progress: Double
    get() = get(Activity.Task)!!.progress
    set(value) {
        get(Activity.Task)!!.progress = value
    }
