/*
 * Copyright (c) 2021, Valaphee.
 * All rights reserved.
 */

plugins {
    application
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation("com.google.inject:guice:5.0.1")
    implementation("io.netty.incubator:netty-incubator-codec-http3:0.0.10.Final") { exclude("io.netty.incubator", "netty-incubator-codec-quic") }
    implementation("io.netty.incubator:netty-incubator-codec-native-quic:0.0.24.Final:windows-x86_64")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0-RC2")
}

tasks {
    shadowJar {
        archiveName = "tead.jar"
    }
}

application {
    mainClass.set("com.valaphee.tead.MainKt")
}
