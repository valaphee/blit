/*
 * Copyright (c) 2021-2022, Valaphee.
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

package com.valaphee.blit.source.dav

import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.blit.progress
import com.valaphee.blit.source.AbstractEntry
import com.valaphee.blit.source.NotFoundError
import com.valaphee.blit.util.transferToWithProgress
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readBytes
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.pool.ByteArrayPool
import java.io.InputStream
import java.io.OutputStream
import java.net.URLDecoder
import java.util.UUID
import kotlin.coroutines.coroutineContext

/**
 * @author Kevin Ludwig
 */
class DavEntry(
    private val source: DavSource,
    override val path: String,
    private val prop: Multistatus.Response.Propstat.Prop
) : AbstractEntry<DavEntry>() {
    override val size get() = prop.getcontentlength
    override val modifyTime get() = prop.getlastmodified?.time ?: 0
    override val directory get() = prop.resourcetype?.collection != null

    override suspend fun makeDirectory(name: String) = TODO()

    override suspend fun list(): List<DavEntry> = if (directory) {
        val path = if (path.startsWith('/')) path.substring(1) else path // Unix path correction, "." ("") and "/" are the same
        val httpResponse = source.httpClient.request("${source._url}/$path") { method = httpMethodPropfind }
        when (httpResponse.status) {
            HttpStatusCode.MultiStatus -> {
                val href = httpResponse.request.url.encodedPath
                xmlMapper.readValue<Multistatus>(httpResponse.readBytes()).response.filter { it.href != href }.mapNotNull {
                    val subPath = URLDecoder.decode(it.href.removePrefix(source.path), "UTF-8")
                    it.propstat.find { it.status == "HTTP/1.1 200 OK" }?.prop?.let { DavEntry(source, subPath, it) }
                }
            }
            HttpStatusCode.NotFound -> throw NotFoundError(path)
            else -> TODO()
        }
    } else emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        val httpResponse = source.httpClient.get("${source._url}/$path")
        when (httpResponse.status) {
            HttpStatusCode.OK -> httpResponse.bodyAsChannel().transferToWithProgress(stream, httpResponse.contentLength() ?: size)
            HttpStatusCode.NotFound -> throw NotFoundError(path)
        }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) {
        check(directory)

        if (source.nextcloud && length > source.nextcloudUploadChunkSize) {
            val id = "blit-${UUID.randomUUID()}"
            source.httpClient.request("${source.url}/uploads/${source.username}/$id") { method = httpMethodMkcol }

            val coroutineContext = coroutineContext
            val chunkCount = ceil(length / source.nextcloudUploadChunkSize.toDouble())
            for (i in 0..chunkCount) {
                source.httpClient.put("${source.url}/uploads/${source.username}/$id/${i * source.nextcloudUploadChunkSize}") { setBody(stream.readNBytes(source.nextcloudUploadChunkSize.toInt())) }
                coroutineContext.progress = i / chunkCount.toDouble()
            }

            source.httpClient.request("${source.url}/uploads/${source.username}/$id/.file") {
                method = httpMethodMove
                headers { this[HttpHeaders.Destination] = URLBuilder("${source._url}/$path/$name").buildString() }
            }
        } else {
            val coroutineContext = coroutineContext
            source.httpClient.put("${source._url}/$path/$name") {
                setBody(object : OutgoingContent.WriteChannelContent() {
                    override val contentLength get() = length

                    override suspend fun writeTo(channel: ByteWriteChannel) {
                        val buffer = ByteArrayPool.borrow()
                        try {
                            var readSum = 0L
                            while (true) {
                                val read = stream.read(buffer, 0, buffer.size)
                                if (read == -1) break
                                if (read > 0) {
                                    channel.writeFully(buffer, 0, read)
                                    readSum += read
                                }
                                coroutineContext.progress = (readSum / length.toDouble())
                            }
                        } finally {
                            ByteArrayPool.recycle(buffer)
                        }
                    }
                })
            }
        }
    }

    override suspend fun rename(path: String) {
        source.httpClient.request("${source._url}/${this.path}") {
            method = httpMethodMove
            headers { this[HttpHeaders.Destination] = URLBuilder("${source._url}/$path").buildString() }
        }
    }

    override suspend fun delete() {
        source.httpClient.delete("${source._url}/$path")
    }

    companion object {
        private fun ceil(value: Double): Int {
            val valueInt = (value + 1).toInt()
            return if (value >= valueInt) valueInt else valueInt - 1
        }
    }
}
