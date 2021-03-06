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

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Provides
import com.google.inject.Singleton
import com.valaphee.blit.config.Config
import com.valaphee.blit.data.DataModule
import com.valaphee.blit.locale.Locale
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import javafx.scene.image.Image
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import java.io.File
import kotlin.reflect.KClass
import kotlin.system.exitProcess

/**
 * @author Kevin Ludwig
 */
class Main : App(Image(Main::class.java.getResourceAsStream("/app.png")), MainView::class)

fun main(arguments: Array<String>) {
    SvgImageLoaderFactory.install()

    FX.dicontainer = object : DIContainer {
        private val injector = Guice.createInjector(DataModule(File(System.getProperty("user.home"), ".valaphee/blit")), object : AbstractModule() {
            @Singleton
            @Provides
            fun locale(config: Config, locales: Map<String, @JvmSuppressWildcards Locale>) = locales[config.locale] ?: locales["en_US"]
        })

        override fun <T : Any> getInstance(type: KClass<T>) = injector.getInstance(type.java)
    }

    launch<Main>(arguments)

    exitProcess(0)
}
