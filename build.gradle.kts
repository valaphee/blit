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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.palantir.git-version") version "0.12.3"
    id("edu.sc.seis.launch4j") version "2.5.0"
    kotlin("jvm") version "1.5.31"
    id("org.openjfx.javafxplugin") version "0.0.10"
    signing
}

subprojects {
    apply(plugin = "com.palantir.git-version")
    apply(plugin = "kotlin")
    apply(plugin = "signing")

    repositories {
        mavenCentral()
        mavenLocal()
    }

    group = "com.valaphee"
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    val details = versionDetails()
    //version = "${details.lastTag}.${details.commitDistance}"

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "16"
            targetCompatibility = "16"
        }

        withType<KotlinCompile>().configureEach { kotlinOptions { jvmTarget = "16" } }

        withType<Test> { useJUnitPlatform() }
    }

    signing { useGpgCmd() }
}
