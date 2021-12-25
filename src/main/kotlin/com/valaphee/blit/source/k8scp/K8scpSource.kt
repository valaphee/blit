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

import com.valaphee.blit.source.NotFoundException
import com.valaphee.blit.source.Source
import com.valaphee.blit.source.scp.parseLsEntry
import io.kubernetes.client.Copy
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.util.Config
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * @author Kevin Ludwig
 */
class K8scpSource(
    private val namespace: String,
    private val pod: String
) : Source<K8scpEntry> {
    override val home: String
        get() = if (namespace.isNotEmpty() && pod.isNotEmpty()) {
            val process = copy.exec(namespace, pod, arrayOf("pwd", toString()), false)
            val home = BufferedReader(InputStreamReader(process.inputStream)).use { it.readLine() }
            process.waitFor()
            home
        } else "/"

    override suspend fun get(path: String): K8scpEntry {
        val (namespace, pod, path) = getNamespacePodAndPath(path)
        return if (namespace != null && pod != null) {
            val process = copy.exec(namespace, pod, arrayOf("stat", "--format", "%A 0 %U %G %s %y %n", path), false)
            val attributes = BufferedReader(InputStreamReader(process.inputStream)).use { parseLsEntry(it.readText())?.second }
            if (process.waitFor() == 0) attributes?.let { K8scpEntry(this, path!!, it) } ?: throw NotFoundException(path!!) else throw NotFoundException(path!!)
        } else K8scpEntry(this, path!!, K8scpEntry.namespaceOrPodAttributes)
    }

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

    override fun close() = Unit

    companion object {
        private val apiClient = Config.defaultClient()
        internal val copy = Copy(apiClient)
        internal val coreV1Api = CoreV1Api(apiClient)
    }
}
