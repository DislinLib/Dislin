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

package com.kodingking.dislin.io.rest.core.client

import com.kodingking.dislin.io.rest.core.RestResponse
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import kotlinx.coroutines.delay

/**
 * Rate limited REST client
 *
 * @author Koding
 * @since 0.1-PRE
 */
open class RateLimitedRestClient(
    token: String,
    type: EnumAuthType = EnumAuthType.BOT
) : AuthRestClient(token, type) {

    /**
     * The default request builder
     */
    override val defaultBuilder: HttpRequestBuilder.() -> Unit = {
        // Add precision header
        header("X-RateLimit-Precision", "millisecond")

        // Invoke the parent's builder
        super.defaultBuilder.invoke(this)
    }
    /**
     * The default rate limit for this route
     */
    private val defaultRateLimit = RateLimit()

    /**
     * Run a rate limited request
     *
     * @param request   The request function
     * @param rateLimit The rate limit we're using for this route
     *
     * @author Koding
     * @since 0.1-PRE
     */
    private suspend fun <T : Any> runLimited(
        request: suspend () -> RestResponse<T>,
        rateLimit: RateLimit = defaultRateLimit
    ): RestResponse<T> {
        // Run the request
        val response = request()

        // Read the headers
        rateLimit.remaining = response.headers["X-RateLimit-Remaining"]?.toIntOrNull() ?: rateLimit.remaining
        rateLimit.resetAfter = response.headers["X-RateLimit-Reset-After"]?.toDoubleOrNull() ?: rateLimit.resetAfter

        // Check if we've been rate limited
        if (response.code == 429 || rateLimit.remaining == 0) {
            // Wait until it resets
            delay((rateLimit.resetAfter * 1000L).toLong())

            // Re-run the request
            return runLimited(request, rateLimit)
        }

        // Otherwise return the response
        return response
    }

    /**
     * The rate limit structure
     *
     * @author Koding
     * @since 0.1-PRE
     */
    data class RateLimit(
        var remaining: Int = -1,
        var resetAfter: Double = -1.0
    )

}
