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
import com.valaphee.blit.source.AbstractSource
import com.valaphee.blit.source.NotFoundException
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BasicAuthCredentials
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.Json
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * @author Kevin Ludwig
 */
class DavSource(
    name: String,
    internal val url: String,
    internal val username: String,
    private val password: String,
    internal val nextcloud: Boolean,
    internal val nextcloudUploadChunkSize: Long
) : AbstractSource<DavEntry>(name) {
    internal val httpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    sslSocketFactory(SSLContext.getInstance("SSL").apply { init(null, trustManagers, java.security.SecureRandom()) }.socketFactory, trustManagers[0] as X509TrustManager)
                    hostnameVerifier { _, _ -> true }
                }
            }
            expectSuccess = false
            install(HttpTimeout) { socketTimeoutMillis = 30 * 1000 }
            install(HttpCookies)
            Auth {
                basic {
                    sendWithoutRequest { true }
                    credentials { BasicAuthCredentials(this@DavSource.username, this@DavSource.password) }
                }
            }
            Json {
                serializer = JacksonSerializer(xmlMapper)
                accept(ContentType.Application.Xml)
            }
        }
    }
    internal val _url get() = if (nextcloud) "${url}/files/${username}" else url

    override val home get() = "/"

    override suspend fun get(path: String): DavEntry {
        val path = if (path.startsWith('/')) path.substring(1) else path // Unix path correction, "." ("") and "/" are the same
        val httpResponse = httpClient.request<HttpResponse>("$_url/$path") { method = httpMethodPropfind }
        return when (httpResponse.status) {
            HttpStatusCode.MultiStatus -> {
                val href = httpResponse.request.url.encodedPath
                xmlMapper.readValue<Multistatus>(httpResponse.readBytes()).response.find { it.href.equals(href, true) }?.propstat?.find { it.status == "HTTP/1.1 200 OK" }?.prop?.let { DavEntry(this, path, it) } ?: TODO()
            }
            HttpStatusCode.NotFound -> throw NotFoundException(path)
            else -> TODO()
        }
    }

    companion object {
        private val trustManagers = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

            override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        })
    }
}
