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

package com.kodingking.dislin.util.extensions

import com.kodingking.dislin.io.rest.core.RestResponse
import com.kodingking.dislin.io.rest.core.client.RateLimitedException
import com.kodingking.dislin.util.content.gson
import com.kodingking.dislin.util.content.readAs
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText

/**
 * Reads a HTTP response and parses the content
 * into our custom contained class
 *
 * @author Koding
 * @since 0.1-PRE
 */
suspend inline fun <reified T : Any> HttpResponse.asResponse(): RestResponse<T> {
    // Check the status code
    when (status.value) {
        // Rate limited
        429 -> throw RateLimitedException()

        // Fallback
        else -> {}
    }

    // Read the JSON
    val json = gson.toJsonTree(readText(Charsets.UTF_8))

    // Read the JSON as the type
    val data = try {
        // Read it
        json.readAs<T>()
    } catch (e: Exception) {
        // Otherwise null
        null
    }

    // Return a response
    return RestResponse(data, json, status.value, headers)
}
