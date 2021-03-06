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

package com.valaphee.blit.config

import com.google.inject.Injector
import com.google.inject.Key
import com.valaphee.blit.locale.Locale
import tornadofx.Fragment
import tornadofx.action
import tornadofx.button
import tornadofx.chooseDirectory
import tornadofx.combobox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.textfield
import tornadofx.validator
import java.io.File

/**
 * General configuration tab
 *
 * @author Kevin Ludwig
 */
class ConfigViewGeneral : Fragment("General") {
    private val locale by di<Locale>()
    private val configModel by di<Config.Model>()
    private val injector by di<Injector>()

    override val root = form {
        fieldset(locale["config.general.appearance.text"]) {
            field(locale["config.general.theme.text"]) {
                combobox(configModel.theme, Config.Theme.values().toList()) { cellFormat { text = locale["config.general.theme.${it.key}"] } }
            }
            field(locale["config.general.locale.text"]) {
                val locales = injector.getInstance(object : Key<Map<String, @JvmSuppressWildcards Locale>>() {})
                combobox(configModel.locale, locales.keys.toList()) { cellFormat { text = locales[it]!!["name"] } }
            }
            field(locale["config.general.data_size_unit.text"]) { combobox(configModel.dataSizeUnit, Config.DataSizeUnit.values().toList()) { cellFormat { text = locale["config.general.data_size_unit.${it.key}"] } } }
        }
        fieldset(locale["config.general.path.text"]) {
            field(locale["config.general.temporary_path.text"]) {
                textfield(configModel.temporaryPath) { validator { if (it.isNullOrBlank() || !File(it).isDirectory) error(locale["config.general.temporary_path.invalid"]) else null } }
                button("...") {
                    action {
                        val parentPath = if (configModel.temporaryPath.value.isEmpty()) null else File(configModel.temporaryPath.value).parentFile
                        chooseDirectory("Select Private Key", if (parentPath?.isDirectory == true) parentPath else null)?.let { configModel.temporaryPath.value = it.absolutePath }
                    }
                }
            }
        }
    }
}
