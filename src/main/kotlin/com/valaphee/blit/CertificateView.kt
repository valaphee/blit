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

package com.valaphee.blit

import com.valaphee.blit.config.Config
import com.valaphee.blit.locale.Locale
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetroStyleClass
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.readonlyColumn
import tornadofx.tab
import tornadofx.tableview
import tornadofx.tabpane
import tornadofx.toObservable
import tornadofx.vbox
import tornadofx.vgrow
import java.security.cert.X509Certificate

/**
 * @author Kevin Ludwig
 */
class CertificateView(
    private val chain: Array<out X509Certificate>,
    private val valid: Boolean
) : View("Certificate") {
    private val locale by di<Locale>()
    private val _config by di<Config>()

    override val root = vbox {
        _config.theme.apply(this)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        setPrefSize(500.0, 600.0)

        tabpane {
            vgrow = Priority.ALWAYS
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            chain.forEach {
                tab(X500Name(it.subjectX500Principal.name).getRDNs(BCStyle.CN)[0].first.value.toString()) {
                    tableview(
                        listOf<Pair<String, Any>>(
                            "Version" to it.version,
                            "Serial number" to it.serialNumber.toString(16),
                            "Signature algorithm" to it.sigAlgName,
                            "Issuer" to it.issuerX500Principal,
                            "Valid from" to it.notBefore,
                            "Valid to" to it.notAfter,
                            "Subject" to it.subjectX500Principal
                        ).toObservable()
                    ) {
                        readonlyColumn("Field", Pair<String, Any>::first) { isReorderable = false }
                        readonlyColumn("Value", Pair<String, Any>::second) { isReorderable = false }

                        setSortPolicy { false }
                    }
                }
            }
        }
        buttonbar { button(locale["certificate.ok.text"]) { action { (scene.window as Stage).close() } } }
    }
}
