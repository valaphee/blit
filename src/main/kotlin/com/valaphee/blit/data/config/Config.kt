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

package com.valaphee.blit.data.config

import com.fasterxml.jackson.annotation.JsonValue
import com.valaphee.blit.source.SourceConfig
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel
import tornadofx.asObservable
import java.text.StringCharacterIterator
import kotlin.math.abs

/**
 * @author Kevin Ludwig
 */
class Config(
    locale: String,
    dataSizeUnit: DataSizeUnit,
    temporaryPath: String,
    sources: List<SourceConfig>
) {
    enum class DataSizeUnit(
        @get:JsonValue val key: String,
        val format: (Long) -> String
    ) {
        BYTES("bytes", { it.toString() }),
        IEC("iec", {
            val sizeAbs = if (it == Long.MIN_VALUE) Long.MAX_VALUE else abs(it)
            if (sizeAbs < 1024) "$it B" else {
                var sizeVar = sizeAbs
                val suffix = StringCharacterIterator("KMGTPE")
                var i = 40
                while (i >= 0 && sizeAbs > 0xFFFCCCCCCCCCCCCL shr i) {
                    sizeVar = sizeVar shr 10
                    suffix.next()
                    i -= 10
                }
                sizeVar *= java.lang.Long.signum(it)
                String.format("%.1f %ciB", sizeVar / 1024.0, suffix.current())
            }
        }),
        SI("si", {
            var sizeVar = it
            if (-1000 < sizeVar && sizeVar < 1000) "$sizeVar B" else {
                val suffix = StringCharacterIterator("kMGTPE")
                while (sizeVar <= -999950 || sizeVar >= 999950) {
                    sizeVar /= 1000
                    suffix.next()
                }
                String.format("%.1f %cB", sizeVar / 1000.0, suffix.current())
            }
        })
    }

    val localeProperty = SimpleStringProperty(locale)
    val dataSizeUnitProperty = SimpleObjectProperty(dataSizeUnit)
    val temporaryPathProperty = SimpleStringProperty(temporaryPath)
    val sourcesProperty = SimpleListProperty(sources.asObservable())

    class Model : ItemViewModel<Config>() {
        val locale = bind(Config::localeProperty)
        val dataSizeUnit = bind(Config::dataSizeUnitProperty)
        val temporaryPath = bind(Config::temporaryPathProperty)
        val sources = bind(Config::sourcesProperty)
    }
}
