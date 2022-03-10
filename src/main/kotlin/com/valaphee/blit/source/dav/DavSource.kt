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
import com.valaphee.blit.source.NotFoundError
import com.valaphee.blit.source.Source
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.json.JacksonSerializer
import io.ktor.client.plugins.json.JsonPlugin
import io.ktor.client.request.request
import io.ktor.client.statement.readBytes
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.encodedPath
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * @author Kevin Ludwig
 */
class DavSource(
    internal val url: String,
    internal val username: String,
    private val password: String,
    internal val nextcloud: Boolean,
    internal val nextcloudUploadChunkSize: Long
) : Source<DavEntry> {
    internal val httpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    sslSocketFactory(socketFactory, trustManagers[0])
                    hostnameVerifier { _, _ -> true }
                }
            }
            expectSuccess = false
            install(HttpTimeout) { socketTimeoutMillis = 60_000L }
            install(HttpCookies)
            install(Auth) {
                basic {
                    sendWithoutRequest { true }
                    credentials { BasicAuthCredentials(this@DavSource.username, this@DavSource.password) }
                }
            }
            install(JsonPlugin) {
                serializer = JacksonSerializer(xmlMapper)
                accept(ContentType.Application.Xml)
            }
        }
    }
    internal val _url = if (nextcloud) "${url}/files/${username}" else url
    internal val path = URLBuilder(_url).encodedPath

    override val home get() = "/"

    override suspend fun get(path: String): DavEntry {
        val path = if (path.startsWith('/')) path.substring(1) else path // Unix path correction, "." ("") and "/" are the same
        val httpResponse = httpClient.request("$_url/$path") { method = httpMethodPropfind }
        return when (httpResponse.status) {
            HttpStatusCode.MultiStatus -> {
                val href = httpResponse.request.url.encodedPath
                xmlMapper.readValue<Multistatus>(httpResponse.readBytes()).response.find { it.href.equals(href, true) }?.propstat?.find { it.status == "HTTP/1.1 200 OK" }?.prop?.let { DavEntry(this, path, it) } ?: TODO()
            }
            HttpStatusCode.NotFound -> throw NotFoundError(path)
            else -> TODO()
        }
    }

    override fun close() {
        httpClient.close()
    }

    companion object {
        private val trustManagers = arrayOf<X509TrustManager>(object : X509TrustManager {
            private val parent = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply { init(null as KeyStore?) }.trustManagers.find { it is X509TrustManager } as X509TrustManager

            override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) = parent.checkClientTrusted(chain, authType)

            override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

            override fun getAcceptedIssuers() = parent.acceptedIssuers
        })
        private val socketFactory = SSLContext.getInstance("TLS").apply { init(null, trustManagers, SecureRandom()) }.socketFactory
    }
}
