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

package com.valaphee.blit.dav

import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.blit.AbstractEntry
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.OutputStream

/**
 * @author Kevin Ludwig
 */
class DavEntry(
    private val davSource: DavSource,
    private val path: String,
    override val name: String,
    private val prop: Multistatus.Response.Propstat.Prop
) : AbstractEntry<DavEntry>() {
    override val size get() = prop.getcontentlength
    override val modifyTime get() = 0L
    override val directory get() = prop.resourcetype?.collection != null

    override suspend fun list(): List<DavEntry> {
        val path = this@DavEntry.toString()
        val httpResponse = davSource.httpClient.request<HttpResponse>("${davSource.url}/$path") { method = DavSource.httpMethodPropfind }
        return if (httpResponse.status == HttpStatusCode.MultiStatus) {
            DavSource.xmlMapper.readValue<Multistatus>(httpResponse.readBytes()).response.mapNotNull {
                val name = it.href.removeSuffix("/").split('/').last()
                if (name != this@DavEntry.name) DavEntry(davSource, path, name, it.propstat.first().prop) else null
            }
        } else emptyList()
    }

    override suspend fun transferTo(stream: OutputStream) {
        davSource.httpClient.get<HttpResponse>("${davSource.url}/${this@DavEntry}").content.copyTo(stream)
    }

    override fun toString() = if (name.isEmpty()) path else if (path.endsWith("/")) "$path$name" else "$path/$name"
}
