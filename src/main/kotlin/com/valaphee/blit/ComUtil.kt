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

import com.sun.javafx.tk.TKStage
import javafx.stage.Stage
import org.bridj.Pointer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

val comExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor { thread(false, true, block = it::run) } }

val Stage.hWnd: Pointer<Int>?
    get() {
        return try {
            val tkStage = try {
                javaClass.superclass.getDeclaredMethod("getPeer")
            } catch (_: NoSuchMethodException) {
                javaClass.getMethod("impl_getPeer")
            }.apply { isAccessible = true }.invoke(this) as TKStage
            val platformWindow = tkStage.javaClass.getDeclaredMethod("getPlatformWindow").apply { isAccessible = true }.invoke(tkStage)
            Pointer.pointerToAddress(platformWindow.javaClass.getMethod("getNativeHandle").apply { isAccessible = true }.invoke(platformWindow) as Long) as Pointer<Int>
        } catch (ex: Throwable) {
            ex.printStackTrace()
            null
        }
    }
