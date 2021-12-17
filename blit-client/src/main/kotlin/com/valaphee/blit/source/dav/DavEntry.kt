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

package com.valaphee.blit.source.dav

import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.blit.source.AbstractEntry
import com.valaphee.blit.util.progress
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.jvm.javaio.copyTo
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
    private val davSource: DavSource,
    private val path: String,
    private val prop: Multistatus.Response.Propstat.Prop
) : AbstractEntry<DavEntry>() {
    override val name = path.removeSuffix("/").split('/').last()
    override val size get() = prop.getcontentlength
    override val modifyTime get() = prop.getlastmodified?.time ?: 0
    override val directory get() = prop.resourcetype?.collection != null

    override suspend fun list() = if (directory) {
        val httpResponse = davSource.httpClient.request<HttpResponse>("${davSource._url}/$path") { method = httpMethodPropfind }
        if (httpResponse.status == HttpStatusCode.MultiStatus) {
            xmlMapper.readValue<Multistatus>(httpResponse.readBytes()).response.mapNotNull {
                val name = URLDecoder.decode(it.href.removeSuffix("/").split('/').last(), "UTF-8")
                if (name != this.name) DavEntry(davSource, "${path}/${name}", it.propstat.first().prop) else null
            }
        } else emptyList()
    } else emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        val httpResponse = davSource.httpClient.get<HttpResponse>("${davSource._url}/$path")
        val buffer = ByteArrayPool.borrow()
        val length = httpResponse.contentLength()
        try {
            var copied = 0L
            while (true) {
                val bytes = httpResponse.content.readAvailable(buffer, 0, buffer.size)
                if (bytes == -1) break
                if (bytes > 0) {
                    stream.write(buffer, 0, bytes)
                    copied += bytes
                }
                length?.let { coroutineContext.progress = copied / it.toDouble() }
            }
            coroutineContext.progress = 1.0
        } finally {
            ByteArrayPool.recycle(buffer)
        }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) {
        if (davSource.nextcloud && length > chunkSize) {
            val id = UUID.randomUUID()
            davSource.httpClient.request<Unit>("${davSource.url}/uploads/${davSource.username}/blit-$id") { method = httpMethodMkcol }

            /*val jobs = mutableListOf<Job>()*/
            for (i in 0..ceil(length / chunkSize.toDouble())) /*jobs += ioScope.launch { */davSource.httpClient.put<Unit>("${davSource.url}/uploads/${davSource.username}/blit-$id/${i * chunkSize}") { body = stream.readNBytes(chunkSize.toInt()) }/* }*/
            /*jobs.joinAll()*/

            davSource.httpClient.request<Unit>("${davSource.url}/uploads/${davSource.username}/blit-$id/.file") {
                method = httpMethodMove
                headers { this["Destination"] = "${davSource._url}/$path/$name" }
            }
        } else davSource.httpClient.put<Unit>("${davSource._url}/$path/$name") { body = InputStreamContent(stream, length) }
    }

    override fun toString() = path

    internal class InputStreamContent(
        private val stream: InputStream,
        private val length: Long
    ) : OutgoingContent.WriteChannelContent() {
        override val contentLength get() = length

        override suspend fun writeTo(channel: ByteWriteChannel) {
            stream.copyTo(channel, length)
        }
    }

    companion object {
        private const val chunkSize = 10485760L

        fun ceil(value: Double): Int {
            val valueInt = (value + 1).toInt()
            return if (value >= valueInt) valueInt else valueInt - 1
        }
    }
}

