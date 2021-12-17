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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.valaphee.blit.source.AbstractSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BasicAuthCredentials
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.Json
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * @author Kevin Ludwig
 */
@JsonTypeName("dav")
class DavSource(
    name: String,
    @get:JsonProperty("url") val url: String,
    @get:JsonProperty("username") val username: String,
    @get:JsonProperty("password") val password: String,
    @get:JsonProperty("nextcloud") val nextcloud: Boolean
) : AbstractSource<DavEntry>(name) {
    @get:JsonIgnore internal val httpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    sslSocketFactory(SSLContext.getInstance("SSL").apply { init(null, trustManagers, java.security.SecureRandom()) }.socketFactory, trustManagers[0] as X509TrustManager)
                    hostnameVerifier { _, _ -> true }
                }
            }
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
    @get:JsonIgnore internal val _url get() = if (nextcloud) "${url}/files/${username}" else url

    override val home get() = "." // TODO: pwd

    override suspend fun isValid(path: String) = httpClient.request<HttpResponse>("$_url/$path") { method = httpMethodPropfind }.status == HttpStatusCode.MultiStatus

    override suspend fun get(path: String) = DavEntry(this, path, httpClient.request<Multistatus>("$_url/$path") { method = httpMethodPropfind }.response.first().propstat.first().prop) // TODO

    companion object {
        private val trustManagers = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

            override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        })
    }
}
