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

package com.valaphee.blit.data.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.google.inject.Singleton
import com.valaphee.blit.data.Data
import com.valaphee.blit.data.DataType
import com.valaphee.blit.source.Source
import com.valaphee.blit.source.local.LocalSource
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.lang.Long.signum
import java.text.StringCharacterIterator
import kotlin.math.abs

/**
 * @author Kevin Ludwig
 */
@Singleton
@DataType("config")
class Config(
    @get:JsonProperty("data_size_unit") var dataSizeUnit: DataSizeUnit = DataSizeUnit.IEC,
    @get:JsonProperty("sources") @get:JsonDeserialize(using = SourceObservableListDeserializer::class) val sources: ObservableList<Source<*>> = FXCollections.observableArrayList(LocalSource("local"))
) : Data {
    enum class DataSizeUnit(
        val format: (Long) -> String
    ) {
        @JsonProperty("iec") IEC({
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
        @JsonProperty("si") SI({
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

    companion object {
        class SourceObservableListDeserializer : JsonDeserializer<ObservableList<Source<*>>>() {
            override fun deserialize(parser: JsonParser, context: DeserializationContext): ObservableList<Source<*>> = FXCollections.observableList(parser.readValueAs<List<Source<*>>>(object : TypeReference<List<Source<*>>>() {}))
        }
    }
}
