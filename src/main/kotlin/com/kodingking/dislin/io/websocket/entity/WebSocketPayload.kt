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

package com.kodingking.dislin.io.websocket.entity

import com.google.gson.annotations.SerializedName

/**
 * Parent class for all web socket
 * payloads to use
 *
 * @author Koding
 * @since 0.1-PRE
 */
open class WebSocketPayload

/**
 * Payload for the hello opcode
 *
 * @author Koding
 * @since 0.1-PRE
 */
data class HelloPayload(
    val heartbeatInterval: Long
) : WebSocketPayload()

/**
 * Payload for the identify opcode
 *
 * @author Koding
 * @since 0.1-PRE
 *
 * TODO: Add presence
 */
data class IdentifyPayload(
    val token: String,
    val properties: IdentifyConnectionProperties,
    val compress: Boolean? = false,
    val largeThreshold: Int? = 50,
    val sharding: List<Int>? = null,
    val guildSubscriptions: Boolean = true
) : WebSocketPayload()

/**
 * Connection properties field for the
 * identify payload
 *
 * @author Koding
 * @since 0.1-PRE
 */
data class IdentifyConnectionProperties(
    @SerializedName("\$os") val os: String,
    @SerializedName("\$browser") val browser: String,
    @SerializedName("\$device") val device: String
)

/**
 * Payload for the resume opcode
 *
 * @author Koding
 * @since 0.1-PRE
 */
data class ResumePayload(
    val token: String,
    val sessionId: String,
    @SerializedName("seq") val sequence: Int
) : WebSocketPayload()
