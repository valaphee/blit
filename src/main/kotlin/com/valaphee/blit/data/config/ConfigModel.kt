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

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import javafx.beans.property.SimpleListProperty
import tornadofx.ItemViewModel
import tornadofx.asObservable
import tornadofx.toProperty
import java.io.File

/**
 * @author Kevin Ludwig
 */
class ConfigModel @Inject constructor(
    config: Config
) : ItemViewModel<Config>(config) {
    private val objectMapper by di<ObjectMapper>()

    val dataSizeUnit = bind { config.dataSizeUnit.toProperty() }
    val sources = bind { SimpleListProperty(config.sources.asObservable()) }

    override fun onCommit() {
        objectMapper.writeValue(File(File("data").also(File::mkdir), "config.json"), Config(dataSizeUnit.value, sources.value))
    }
}
