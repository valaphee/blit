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

import com.google.inject.Injector
import com.google.inject.Key
import com.valaphee.blit.data.locale.Locale
import tornadofx.Fragment
import tornadofx.combobox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form

/**
 * @author Kevin Ludwig
 */
class ConfigViewGeneral : Fragment("General") {
    private val locale by di<Locale>()
    private val configModel by di<ConfigModel>()
    private val injector by di<Injector>()

    override val root = form {
        fieldset {
            val locales = injector.getInstance(object : Key<Map<String, @JvmSuppressWildcards Locale>>() {})
            field(locale["config.general.locale.text"]) { combobox(configModel.locale, locales.keys.toList()) { cellFormat { text = locales[it]!!["name"] } } }
            field(locale["config.general.data_size_unit.text"]) { combobox(configModel.dataSizeUnit, Config.DataSizeUnit.values().toList()) }
        }
    }
}
