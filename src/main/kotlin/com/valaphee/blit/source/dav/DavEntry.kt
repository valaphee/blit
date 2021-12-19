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

package com.valaphee.blit.source.dav

import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.blit.progress
import com.valaphee.blit.source.AbstractEntry
import com.valaphee.blit.source.NotFoundException
import com.valaphee.blit.source.transferToWithProgress
import io.ktor.client.request.delete
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
            var first = true
            xmlMapper.readValue<Multistatus>(httpResponse.readBytes()).response.mapNotNull {
                if (first) {
                    first = false
                    null
                } else {
                    val name = URLDecoder.decode(it.href.removeSuffix("/").split('/').last(), "UTF-8")
                    /*if (name != this.name) */DavEntry(davSource, "${if (path == "/") "" else path}/${name}", it.propstat.first().prop)/* else null*/
                } // TODO: it is not defined in the standard that the first entry has to be the entry itself
            }
        } else emptyList()
    } else emptyList()

    override suspend fun transferTo(stream: OutputStream) {
        val httpResponse = davSource.httpClient.get<HttpResponse>("${davSource._url}/$path")
        when (httpResponse.status) {
            HttpStatusCode.OK -> httpResponse.content.transferToWithProgress(stream, httpResponse.contentLength() ?: size)
            HttpStatusCode.NotFound -> throw NotFoundException(path)
        }
    }

    override suspend fun transferFrom(name: String, stream: InputStream, length: Long) {
        if (davSource.nextcloud && length > chunkSize) {
            val id = "blit-${UUID.randomUUID()}"
            davSource.httpClient.request<Unit>("${davSource.url}/uploads/${davSource.username}/$id") { method = httpMethodMkcol }

            /*val jobs = mutableListOf<Job>()*/
            val chunkCount = ceil(length / chunkSize.toDouble())
            for (i in 0..chunkCount) {
                /*jobs += ioScope.launch { */
                davSource.httpClient.put<Unit>("${davSource.url}/uploads/${davSource.username}/$id/${i * chunkSize}") { body = stream.readNBytes(chunkSize.toInt()) }
                coroutineContext.progress = i / chunkCount.toDouble() // TODO: more precise progress
                /*}*/
            }
            /*jobs.joinAll()*/

            davSource.httpClient.request<Unit>("${davSource.url}/uploads/${davSource.username}/$id/.file") {
                method = httpMethodMove
                headers { this["Destination"] = "${davSource._url}/$path/$name" }
            }
        } else davSource.httpClient.put<Unit>("${davSource._url}/$path/$name") { body = InputStreamContent(stream, length) } // TODO: set progress
    }

    override suspend fun rename(name: String) {
        davSource.httpClient.request<Unit>("${davSource._url}/$path") {
            method = httpMethodMove
            headers { this["Destination"] = "${davSource._url}/${path.substringBeforeLast('/', "")}/$name" }
        }
    }

    override suspend fun delete() {
        davSource.httpClient.delete<Unit>("${davSource._url}/$path")
    }

    override fun toString() = path

    private class InputStreamContent(
        private val stream: InputStream,
        private val length: Long
    ) : OutgoingContent.WriteChannelContent() {
        override val contentLength get() = length

        override suspend fun writeTo(channel: ByteWriteChannel) {
            stream.copyTo(channel, length)
        }
    }

    companion object {
        private const val chunkSize = 10L * 1024 * 1024

        private fun ceil(value: Double): Int {
            val valueInt = (value + 1).toInt()
            return if (value >= valueInt) valueInt else valueInt - 1
        }
    }
}
