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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.google.inject.Singleton
import com.valaphee.blit.data.Data
import com.valaphee.blit.data.DataType
import com.valaphee.blit.source.Source
import com.valaphee.blit.source.local.LocalSource
import javafx.beans.property.SimpleListProperty
import tornadofx.ItemViewModel
import tornadofx.toObservable
import tornadofx.toProperty
import java.io.File
import java.lang.Long.signum
import java.text.StringCharacterIterator
import java.util.Locale
import kotlin.math.abs

/**
 * @author Kevin Ludwig
 */
@Singleton
@DataType("config")
class Config(
    @get:JsonProperty("locale") val locale: String = Locale.getDefault().toLanguageTag().replace('-', '_'),
    @get:JsonProperty("data_size_unit") val dataSizeUnit: DataSizeUnit = DataSizeUnit.IEC,
    @get:JsonProperty("temporary_path") val temporaryPath: String = System.getProperty("java.io.tmpdir"),
    @get:JsonProperty("sources") val sources: List<Source<*>> = listOf(LocalSource("local"))
) : Data {
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
                sizeVar *= signum(it)
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

    @Singleton
    class Model @Inject constructor(
        config: Config
    ) : ItemViewModel<Config>(config) {
        private val objectMapper by di<ObjectMapper>()

        val locale = bind { config.locale.toProperty() }
        val dataSizeUnit = bind { config.dataSizeUnit.toProperty() }
        val temporaryPath = bind { config.temporaryPath.toProperty() }
        val sources = bind { SimpleListProperty(config.sources.toObservable()) }

        override fun onCommit() {
            objectMapper.writeValue(File(File("data").also(File::mkdir), "config.json"), Config(locale.value, dataSizeUnit.value, temporaryPath.value, sources.value))
        }
    }
}
