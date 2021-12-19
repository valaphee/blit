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
import com.valaphee.blit.source.NotFoundException
import io.kubernetes.client.Copy
import io.kubernetes.client.openapi.apis.CoreV1Api
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
    override val home: String get() = if (namespace.isNotEmpty() && pod.isNotEmpty()) {
        val process = copy.exec(namespace, pod, arrayOf("pwd", toString()), false)
        val home = BufferedReader(InputStreamReader(process.inputStream)).use { it.readLine() }
        process.waitFor()
        home
    } else "/"

    override suspend fun get(path: String) = stat(path)?.let { K8scpEntry(this, path, it) } ?: throw NotFoundException(path)

    internal fun getNamespacePodAndPath(path: String): Triple<String?, String?, String?> {
        return if (namespace.isNotEmpty()) {
            if (pod.isNotEmpty()) {
                Triple(namespace, pod, path)
            } else {
                val podAndPath = path.split('/', limit = 3)
                val pod = podAndPath.getOrNull(1)
                Triple(namespace, if (pod.isNullOrEmpty()) null else pod, "/${podAndPath.getOrNull(2) ?: ""}")
            }
        } else {
            val namespacePodAndPath = path.split('/', limit = 4)
            val namespace = namespacePodAndPath.getOrNull(1)
            val pod = namespacePodAndPath.getOrNull(2)
            Triple(if (namespace.isNullOrEmpty()) null else namespace, if (pod.isNullOrEmpty()) null else pod, "/${namespacePodAndPath.getOrNull(3) ?: ""}")
        }
    }

    companion object {
        private val apiClient = Config.defaultClient()
        internal val copy = Copy(apiClient)
        internal val coreV1Api = CoreV1Api(apiClient)
    }
}
