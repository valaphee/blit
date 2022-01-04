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

import com.valaphee.blit.data.config.Config
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.text.TextAlignment
import jfxtras.styles.jmetro.JMetroStyleClass
import tornadofx.View
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.label

/**
 * @author Kevin Ludwig
 */
class AboutView : View("About Blit") {
    private val _config by di<Config>()

    override val root = hbox {
        _config.theme.apply(this)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        setPrefSize(300.0, 100.0)
        alignment = Pos.CENTER

        imageview(Image(AboutView::class.java.getResourceAsStream("/app.png")))
        label(
            """
                Blit${AboutView::class.java.`package`.implementationVersion?.let { " $it" } ?: ""}
                Copyright (c) 2021, Valaphee.
            """.trimIndent()
        ) { textAlignment = TextAlignment.CENTER }
    }
}
