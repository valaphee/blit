/*
 * Copyright (c) 2021, Valaphee.
 * All rights reserved.
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
