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

package com.valaphee.blit.source.scp

import com.valaphee.blit.source.Source
import com.valaphee.blit.source.SourceUi
import javafx.event.EventTarget
import javafx.scene.control.TextField
import tornadofx.Field
import tornadofx.action
import tornadofx.button
import tornadofx.chooseFile
import tornadofx.field
import tornadofx.filterInput
import tornadofx.isInt
import tornadofx.passwordfield
import tornadofx.textfield
import java.io.File

/**
 * @author Kevin Ludwig
 */
object ScpSourceUi : SourceUi {
    override val key get() = "scp"
    override val `class` get() = ScpSource::class

    override fun getConfigureUi(eventTarget: EventTarget, source: Source<*>?) = with(eventTarget) {
        val sftpSource = source as? ScpSource
        listOf(
            field("Name") { textfield(source?.name ?: "") },
            field("Host") { textfield(sftpSource?.host ?: "") },
            field("Port") { textfield(sftpSource?.port?.toString() ?: "") { filterInput { it.controlNewText.isInt() } } },
            field("Username") { textfield(sftpSource?.username ?: "") },
            field("Password") { passwordfield(sftpSource?.password ?: "") },
            field("Private Key") {
                val path = textfield(sftpSource?.privateKey ?: "")
                button("...") {
                    action {
                        val parentPath = if (path.text.isEmpty()) null else File(path.text).parentFile
                        chooseFile("Select Private Key", emptyArray(), if (parentPath?.isDirectory == true) parentPath else null).firstOrNull()?.let { path.text = it.absolutePath }
                    }
                }
            }
        )
    }

    override fun getConfigurationFromUi(fields: List<Field>) = ScpSource(
        (fields[0].inputs[0] as TextField).text,
        (fields[1].inputs[0] as TextField).text,
        (fields[2].inputs[0] as TextField).text.toInt(),
        (fields[3].inputs[0] as TextField).text,
        (fields[4].inputs[0] as TextField).text,
        (fields[5].inputs[0] as TextField).text
    )
}
