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

package com.valaphee.blit

import com.valaphee.blit.data.config.Config
import com.valaphee.blit.data.locale.Locale
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetroStyleClass
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.form
import tornadofx.readonlyColumn
import tornadofx.smartResize
import tornadofx.tableview
import tornadofx.toObservable
import java.security.cert.X509Certificate

/**
 * @author Kevin Ludwig
 */
class CertificateView(
    private val chain: Array<out X509Certificate>,
    private val valid: Boolean
) : View("Certificate${if (!valid) " (Invalid)" else ""}") {
    private val locale by di<Locale>()
    private val _config by di<Config>()

    override val root = form {
        _config.theme.apply(this)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        prefWidth = 500.0

        val certificate = chain.first()
        tableview(listOf(
            "Version" to "V${certificate.version}",
            "Serial number" to certificate.serialNumber.toString(16),
            "Signature algorithm" to certificate.sigAlgName,
            "Issuer" to certificate.issuerDN,
            "Valid from" to certificate.notBefore,
            "Valid to" to certificate.notAfter,
            "Subject" to certificate.subjectDN
        ).toObservable()) {
            readonlyColumn("Field", Pair<String, Any>::first)
            readonlyColumn("Value", Pair<String, Any>::second)
            smartResize()

            setSortPolicy { false }
        }
        buttonbar { button(locale["rename.ok.text"]) { action { (scene.window as Stage).close() } } }
    }
}