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
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.0")
    implementation("com.fasterxml.jackson.module:jackson-module-afterburner:2.13.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("com.google.inject:guice:5.0.1")
    implementation("com.nativelibs4java:bridj:0.7.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.3")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.3")
    implementation("de.codecentric.centerdevice:javafxsvg:1.3.0")
    implementation("io.github.classgraph:classgraph:4.8.138")
    implementation("io.ktor:ktor-client-auth:1.6.7")
    implementation("io.ktor:ktor-client-jackson:1.6.7")
    implementation("io.ktor:ktor-client-logging-jvm:1.6.7")
    implementation("io.ktor:ktor-client-okhttp:1.6.7")
    implementation("io.kubernetes:client-java:14.0.0")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.apache.sshd:sshd-netty:2.8.0")
    implementation("org.apache.sshd:sshd-sftp:2.8.0")
    implementation("org.controlsfx:controlsfx:11.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0-RC2")
    implementation("org.jfxtras:jmetro:11.6.15")
}

repositories {
    mavenCentral()
}

application { mainClass.set("com.valaphee.blit.MainKt") }

tasks {
    shadowJar {
        archiveName = "blit-client.jar"
    }
}

javafx { modules("javafx.controls", "javafx.graphics") }

launch4j {
    mainClassName = "com.valaphee.blit.MainKt"
    jarTask = tasks.shadowJar.get()
    icon = "${projectDir}/app.ico"
    copyright = "Copyright (c) 2021, Valaphee"
    jvmOptions = setOf("--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-opens java.base/jdk.internal.misc=ALL-UNNAMED", "-Dio.netty.tryReflectionSetAccessible=true")
    companyName = "Valaphee"
    productName = "Blit"
    /*splashFileName = "${projectDir}/splash.bmp"*/
    copyConfigurable = emptyArray<Any>()
}
