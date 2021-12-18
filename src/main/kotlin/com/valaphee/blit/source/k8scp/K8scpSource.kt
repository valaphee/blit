/*
 * Copyright (c) 2021, Valaphee.
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

package com.valaphee.blit.source.k8scp

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.source.AbstractSource
import io.kubernetes.client.Copy
import io.kubernetes.client.Exec
import io.kubernetes.client.util.Config
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("k8scp")
class K8scpSource(
    name: String,
    @get:JsonProperty("namespace") val namespace: String,
    @get:JsonProperty("pod") val pod: String
) : AbstractSource<K8scpEntry>(name) {
    override val home: String get() {
        val process = exec.exec(namespace, pod, arrayOf("pwd", toString()), false)
        val home = BufferedReader(InputStreamReader(process.inputStream)).use { it.readLine() }
        process.waitFor()
        return home
    }

    override suspend fun isValid(path: String) = stat(path)?.isDirectory ?: false

    override suspend fun get(path: String) = K8scpEntry(this, path, stat(path)!!)

    companion object {
        private val apiClient = Config.defaultClient()
        internal val exec = Exec(apiClient)
        internal val copy = Copy(apiClient)

    }
}
