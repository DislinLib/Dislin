/*
 * Dislin - Kotlin Discord API Wrapper
 * Copyright (c) 2019 Koding
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

package com.kodingking.dislin.io.rest.core

import com.google.gson.FieldNamingPolicy
import com.kodingking.dislin.core.restPath
import com.kodingking.dislin.util.extensions.asResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Base class for the Discord REST client
 *
 * @author Koding
 * @since 0.1-PRE
 */
open class DiscordRestClient {

    companion object {
        /**
         * The rest client
         */
        val client = HttpClient(OkHttp) {
            // Install json
            install(JsonFeature) {
                // Configure serialization
                serializer = GsonSerializer {
                    // Serialize nulls
                    serializeNulls()

                    // Configure pattern
                    setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                }
            }
        }
    }

    /**
     * An additional custom builder for HTTP requests
     */
    open val defaultBuilder: HttpRequestBuilder.() -> Unit = {}

    /**
     * Perform a GET request
     *
     * @param path    The path of the request
     * @param headers The headers for the request
     * @param builder The request builder for additional data
     *
     * @author Koding
     * @since 0.1-PRE
     */
    protected suspend inline fun <reified T : Any> get(
        path: String,
        headers: Map<String, Any?> = hashMapOf(),
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = client.get<HttpResponse>("$restPath/$path") {
        // Apply template
        setup(headers, builder)
    }.asResponse<T>()

    /**
     * Perform a POST request
     *
     * @param path    The endpoint path
     * @param body    The content to send
     * @param headers The headers to add
     * @param builder Custom builder for the request
     *
     * @author Koding
     * @since 0.1-PRE
     */
    protected suspend inline fun <reified T : Any> post(
        path: String,
        body: Any = EmptyContent,
        headers: Map<String, Any?> = hashMapOf(),
        builder: HttpRequestBuilder.() -> Unit = {}
    ) = client.post<HttpResponse>("$restPath/$path") {
        // Apply template
        setup(headers, builder)

        // Add the body
        this.body = body

        // Add content type
        contentType(ContentType.Application.Json)
    }.asResponse<T>()

    /**
     * Setup a basic template for a
     * HTTP Request
     *
     * @param headers The headers to add
     * @param builder The custom request builder
     *
     * @author Koding
     * @since 0.1-PRE
     */
    protected inline fun HttpRequestBuilder.setup(
        headers: Map<String, Any?> = hashMapOf(),
        builder: HttpRequestBuilder.() -> Unit = {}
    ) {
        // Loop through all the headers
        headers.forEach { (key, value) ->
            // Add the header
            header(key, value)
        }

        // Apply custom builder operations
        builder()
        defaultBuilder()
    }

}
