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

package com.valaphee.blit.data.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.google.inject.Singleton
import com.valaphee.blit.data.Data
import com.valaphee.blit.data.DataType
import com.valaphee.blit.source.SourceConfig
import com.valaphee.blit.source.local.LocalSourceConfig
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.Parent
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style
import tornadofx.ItemViewModel
import tornadofx.asObservable
import tornadofx.getValue
import tornadofx.setValue
import java.io.File
import java.text.StringCharacterIterator
import kotlin.math.abs

/**
 * Configuration data
 *
 * @author Kevin Ludwig
 */
@Singleton
@DataType("config")
class Config(
    theme: Theme = Theme.JMetroDark,
    locale: String = "en_US",
    dataSizeUnit: DataSizeUnit = DataSizeUnit.IEC,
    temporaryPath: String = System.getProperty("java.io.tmpdir"),
    sources: List<SourceConfig> = listOf(LocalSourceConfig("local")),
    proxyMode: ProxyMode = ProxyMode.System,
    proxyHost: String = "",
    proxyPort: Int = 0,
    proxyUsername: String = "",
    proxyPassword: String = ""
) : Data {
    /**
     * Themes are applied by calling the apply function in each JavaFX window.
     */
    enum class Theme(
        @get:JsonValue val key: String,
        val apply: (Parent) -> Unit
    ) {
        JavaFX("javafx", {}),
        JMetroLight("jmetro_light", { JMetro(it, Style.LIGHT) }),
        JMetroDark("jmetro_dark", { JMetro(it, Style.DARK) })
    }

    /**
     * Data size unit, formatting works by calling the format function and returning the formatted string.
     */
    enum class DataSizeUnit(
        @get:JsonValue val key: String,
        val format: (Long) -> String
    ) {
        Bytes("bytes", { it.toString() }),
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

    /**
     * Proxy mode
     */
    enum class ProxyMode {
        None,
        System,
        Manual
    }

    @get:JsonIgnore internal val themeProperty = SimpleObjectProperty(theme)
    var theme: Theme by themeProperty

    @get:JsonIgnore internal val localeProperty = SimpleStringProperty(locale)
    var locale: String by localeProperty

    @get:JsonIgnore internal val dataSizeUnitProperty = SimpleObjectProperty(dataSizeUnit)
    var dataSizeUnit: DataSizeUnit by dataSizeUnitProperty

    @get:JsonIgnore internal val temporaryPathProperty = SimpleStringProperty(temporaryPath)
    var temporaryPath: String by temporaryPathProperty

    @get:JsonIgnore internal val sourcesProperty = SimpleListProperty(sources.asObservable())
    var sources: ObservableList<SourceConfig> by sourcesProperty

    @get:JsonIgnore internal val proxyModeProperty = SimpleObjectProperty(proxyMode)
    var proxyMode: ProxyMode by proxyModeProperty

    @get:JsonIgnore internal val proxyHostProperty = SimpleStringProperty(proxyHost)
    var proxyHost: String by proxyHostProperty

    @get:JsonIgnore internal val proxyPortProperty = SimpleIntegerProperty(proxyPort)
    var proxyPort: Int by proxyPortProperty

    @get:JsonIgnore internal val proxyUsernameProperty = SimpleStringProperty(proxyUsername)
    var proxyUsername: String by proxyUsernameProperty

    @get:JsonIgnore internal val proxyPasswordProperty = SimpleStringProperty(proxyPassword)
    var proxyPassword: String by proxyPasswordProperty

    @Singleton
    class Model @Inject constructor(
        config: Config
    ) : ItemViewModel<Config>(config) {
        private val objectMapper by di<ObjectMapper>()

        val theme = bind(Config::themeProperty)
        val locale = bind(Config::localeProperty)
        val dataSizeUnit = bind(Config::dataSizeUnitProperty)
        val temporaryPath = bind(Config::temporaryPathProperty)
        val sources = bind(Config::sourcesProperty)
        val proxyMode = bind(Config::proxyModeProperty)
        val proxyHost = bind(Config::proxyHostProperty)
        val proxyPort = bind(Config::proxyPortProperty)
        val proxyUsername = bind(Config::proxyUsernameProperty)
        val proxyPassword = bind(Config::proxyPasswordProperty)

        override fun onCommit() {
            objectMapper.writeValue(File(File("data").also(File::mkdir), "config.json"), Config(theme.value, locale.value, dataSizeUnit.value, temporaryPath.value, sources, proxyMode.value, proxyHost.value, proxyPort.value, proxyUsername.value, proxyPassword.value))
        }
    }
}
