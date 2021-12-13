/*
 * Copyright (c) 2021, Valaphee.
 * All rights reserved.
 */

rootProject.name = "tead"
file(".").walk().maxDepth(1).filter { it.isDirectory && it.name != rootProject.name && File(it, "build.gradle.kts").exists() }.forEach { include(it.name) }
