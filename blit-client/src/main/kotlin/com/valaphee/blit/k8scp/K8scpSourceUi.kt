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

package com.valaphee.blit.k8scp

import com.valaphee.blit.Source
import com.valaphee.blit.SourceUi
import javafx.event.EventTarget
import javafx.scene.control.TextField
import tornadofx.Field
import tornadofx.field
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
object K8scpSourceUi : SourceUi {
    override val name get() = "K8s CP"
    override val `class` get() = K8scpSource::class

    override fun getFields(eventTarget: EventTarget, source: Source<*>?) = with(eventTarget) {
        val k8scpSource = source as? K8scpSource
        listOf(
            field("Name") { textfield(source?.name ?: "") },
            field ("Namespace") { textfield(k8scpSource?.namespace ?: "") },
            field ("Pod") { textfield(k8scpSource?.pod ?: "") }
        )
    }

    override fun getSource(fields: List<Field>) = K8scpSource(
        (fields[0].inputs[0] as TextField).text,
        (fields[1].inputs[0] as TextField).text,
        (fields[2].inputs[0] as TextField).text
    )
}
