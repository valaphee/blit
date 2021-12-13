/*
 * Copyright (c) 2021, Valaphee.
 * All rights reserved.
 */

package com.valaphee.tead.explorer

import javafx.scene.layout.Priority
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import tornadofx.View
import tornadofx.hgrow
import tornadofx.label
import tornadofx.splitpane
import tornadofx.vbox
import java.io.File

/**
 * @author Kevin Ludwig
 */
class Explorer : View("Explorer") {
    override val root = splitpane {
        JMetro(this, Style.DARK)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        vbox {
            hgrow = Priority.ALWAYS

            label("Local")
            add(Tree(LocalEntry(File(".")), Dispatchers.IO + SupervisorJob()).also { it.startUpdates() })
        }

        vbox {
            hgrow = Priority.ALWAYS

            label("Remote")
            add(Tree(LocalEntry(File(".")), Dispatchers.IO + SupervisorJob()).also { it.startUpdates() })
        }
    }
}
