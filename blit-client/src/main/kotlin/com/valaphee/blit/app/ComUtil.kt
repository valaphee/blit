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

import com.sun.javafx.tk.TKStage
import javafx.stage.Stage
import org.bridj.Pointer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

val comExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor { thread(false, true, block = it::run) } }

val Stage.hWnd: Pointer<Int>? get() {
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
