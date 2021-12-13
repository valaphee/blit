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

plugins {
    application
    id("com.github.johnrengelman.shadow")
    id("edu.sc.seis.launch4j")
    id("org.openjfx.javafxplugin")
}

dependencies {
    implementation("com.google.inject:guice:5.0.1")
    implementation("io.netty.incubator:netty-incubator-codec-http3:0.0.10.Final") { exclude("io.netty.incubator", "netty-incubator-codec-quic") }
    implementation("io.netty.incubator:netty-incubator-codec-native-quic:0.0.24.Final:windows-x86_64")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0-RC2")
    implementation("org.jfxtras:jmetro:11.6.15")
}

tasks {
    shadowJar {
        archiveName = "tead.jar"
    }
}

javafx { modules("javafx.controls", "javafx.graphics") }

application {
    mainClass.set("com.valaphee.tead.MainKt")
}

launch4j {
    mainClassName = "com.valaphee.tead.MainKt"
    jarTask = tasks.shadowJar.get()
    icon = "${projectDir}/app.ico"
    copyright = "Copyright (c) 2021, Valaphee"
    jvmOptions = setOf("--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens java.base/jdk.internal.misc=ALL-UNNAMED", "-Dio.netty.tryReflectionSetAccessible=true")
    companyName = "Valaphee"
    productName = "Tead"
    /*splashFileName = "${projectDir}/splash.bmp"*/
    copyConfigurable = emptyArray<Any>()
}
