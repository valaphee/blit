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

import com.valaphee.blit.data.locale.Locale
import javafx.beans.property.SimpleObjectProperty
import tornadofx.Fragment
import tornadofx.combobox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.onChange

/**
 * @author Kevin Ludwig
 */
class ConfigViewGeneral : Fragment("General") {
    private val locale by di<Locale>()
    private val _config by di<Config>()

    private val dataSizeUnit = SimpleObjectProperty(_config.dataSizeUnit).apply { onChange { it?.let { _config.dataSizeUnit = it } } }

    override val root = form {
        fieldset {
            field(locale["config.general.data_size_unit"]) { combobox(dataSizeUnit, values = Config.DataSizeUnit.values().toList()) }
        }
    }
}
