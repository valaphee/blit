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

package com.valaphee.blit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.AbstractModule
import com.google.inject.Guice
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.io.File
import kotlin.reflect.KClass
import kotlin.system.exitProcess

/**
 * @author Kevin Ludwig
 */
class Main : App(Image(Main::class.java.getResourceAsStream("/app.png")), View::class) {
    override fun start(stage: Stage) {
        stage.setOnCloseRequest {
            if (SystemTray.isSupported()) SystemTray.getSystemTray().add(TrayIcon(Toolkit.getDefaultToolkit().getImage(Main::class.java.getResource("/app.png")).getScaledInstance(16, 16, java.awt.Image.SCALE_DEFAULT)).apply {
                popupMenu = PopupMenu().apply {
                    add(MenuItem("Exit").apply { setOnAction { exitProcess(0) } })
                }
            })
        }
        super.start(stage)
    }
}

fun main(arguments: Array<String>) {
    Platform.setImplicitExit(false)
    SvgImageLoaderFactory.install()

    FX.dicontainer = object : DIContainer {
        private val injector = Guice.createInjector(object : AbstractModule() {
            override fun configure() {
                val configFile = File("config.json")
                bind(Config::class.java).toInstance(if (configFile.exists()) jacksonObjectMapper().readValue<Config>(configFile) else Config().also { jacksonObjectMapper().writeValue(configFile, it) })
            }
        })

        override fun <T : Any> getInstance(type: KClass<T>) = injector.getInstance(type.java)
    }

    launch<Main>(arguments)
}
