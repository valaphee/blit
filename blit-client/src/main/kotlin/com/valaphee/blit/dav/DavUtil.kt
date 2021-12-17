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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.HttpMethod
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal val httpMethodPropfind = HttpMethod("PROPFIND")
internal val httpMethodMkcol = HttpMethod("MKCOL")
internal val httpMethodMove = HttpMethod("MOVE")
internal val xmlMapper = XmlMapper().apply {
    registerModule(AfterburnerModule())
    registerKotlinModule()

    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}

internal fun getTrustAllOkHttpClient(): OkHttpClient {
    val trustManagers = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

        override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

        override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
    })
    return OkHttpClient.Builder()
        .sslSocketFactory(SSLContext.getInstance("SSL").apply { init(null, trustManagers, java.security.SecureRandom()) }.socketFactory, trustManagers[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .build()
}
