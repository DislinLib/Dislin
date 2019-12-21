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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Parent class for all event payloads
 *
 * @author Koding
 * @since 0.1-PRE
 */
open class EventPayload

/**
 * Payload for the ready gateway event
 *
 * TODO: Add user & guild fields
 *
 * @author Koding
 * @since 0.1-PRE
 */
data class ReadyEventPayload(
    @SerializedName("v") val version: Int,
    val user: JsonElement,
    val privateChannels: ArrayList<String>,
    val guilds: JsonArray,
    val sessionId: String,
    val shard: ArrayList<Int>?
)