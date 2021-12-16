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

package com.valaphee.blit.k8scp

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.AbstractSource
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

    override fun isValid(path: String) = stat(path)?.isDirectory ?: false

    override fun get(path: String) = K8scpEntry(this, path, "")

    companion object {
        private val apiClient = Config.defaultClient()
        internal val exec = Exec(apiClient)
        internal val copy = Copy(apiClient)

    }
}
