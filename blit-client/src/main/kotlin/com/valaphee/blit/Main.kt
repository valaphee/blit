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

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Provides
import com.google.inject.Singleton
import com.valaphee.blit.data.DataModule
import com.valaphee.blit.data.config.Config
import com.valaphee.blit.data.locale.Locale
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import javafx.scene.image.Image
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import java.io.File
import kotlin.reflect.KClass

/**
 * @author Kevin Ludwig
 */
class Main : App(Image(Main::class.java.getResourceAsStream("/app.png")), MainView::class)

fun main(arguments: Array<String>) {
    SvgImageLoaderFactory.install()

    val injector = Guice.createInjector(DataModule(), object : AbstractModule() {
        @Singleton
        @Provides
        fun i18n(locales: Map<String, @JvmSuppressWildcards Locale>) = locales["en_us"]
    })

    val configFile = File(File("data").also(File::mkdir), "config.json")
    if (!configFile.exists()) injector.getInstance(ObjectMapper::class.java).writeValue(configFile, injector.getInstance(Config::class.java)) // TODO

    FX.dicontainer = object : DIContainer {
        override fun <T : Any> getInstance(type: KClass<T>) = injector.getInstance(type.java)
    }

    launch<Main>(arguments)
}
