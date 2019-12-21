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

package com.kodingking.dislin.io.websocket

import com.google.gson.FieldNamingPolicy
import com.kodingking.dislin.core.Dislin
import com.kodingking.dislin.core.event.WebSocketClosedEvent
import com.kodingking.dislin.io.websocket.entity.EnumWebSocketOpcode
import com.kodingking.dislin.io.websocket.entity.WebSocketData
import com.kodingking.dislin.io.websocket.entity.WebSocketPayload
import com.kodingking.dislin.io.websocket.listener.DefaultWebSocketListener
import com.kodingking.dislin.util.content.gson
import com.kodingking.dislin.util.content.readAs
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.wss
import io.ktor.client.request.parameter
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.receiveOrNull

/**
 * Instance of a connection to the Discord
 * web socket
 *
 * @author Koding
 * @since 0.1-PRE
 */
@UseExperimental(ExperimentalCoroutinesApi::class, KtorExperimentalAPI::class)
class DiscordWebSocket(
    token: String,
    val dislin: Dislin
) {

    companion object {
        /**
         * The web socket client
         */
        private val client = HttpClient(OkHttp) {
            // Install web sockets
            install(WebSockets)

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
     * Function for sending data to the
     * Discord websocket
     */
    private var send: suspend (ByteArray) -> Unit = {}

    /**
     * Function for closing the connection to
     * the Discord websocket
     */
    private var close: suspend (CloseReason) -> Unit = {}

    /**
     * Instance of the current listener
     */
    private val listener = DefaultWebSocketListener(token)

    /**
     * Connect to the web socket
     *
     * @author Koding
     * @since 0.1-PRE
     */
    suspend fun connect() {
        // Connect to the socket
        client.wss(
            host = "gateway.discord.gg",
            request = {
                // Build the URL
                url {
                    // Add the version
                    parameter("v", 6)

                    // Set the encoding to JSON
                    parameter("encoding", "json")
                }
            }
        ) {
            // Create the send function
            this@DiscordWebSocket.send = {
                // Invoke the actual function
                send(it)
            }

            // Create the close function
            this@DiscordWebSocket.close = {
                // Invoke the actual function
                close(it)
            }

            // Listen for packets
            while (true) {
                // Read all text packets inbound
                val frame = (incoming.receiveOrNull() as? Frame.Text?) ?: continue

                // Decode their text to a web socket
                // data object
                val data = frame.readText().readAs<WebSocketData>()

                // Read the opcode
                val opcode = EnumWebSocketOpcode.values().firstOrNull {
                    // Check if the ID matches
                    it.id == data.opcode
                } ?: continue

                // Fire listener
                listener.onReceived(this@DiscordWebSocket, opcode, data)
            }
        }
    }

    /**
     * Sends a payload to the Discord gateway
     *
     * @param payload The payload to send
     *
     * @author Koding
     * @since 0.1-PRE
     */
    private suspend fun send(payload: WebSocketData) =
        send.invoke(gson.toJson(payload).toByteArray())

    /**
     * Sends a payload to the Discord gateway
     *
     * @param opcode  The opcode for the web socket
     * @param payload The data to send
     *
     * @author Koding
     * @since 0.1-PRE
     */
    suspend fun send(opcode: EnumWebSocketOpcode, payload: WebSocketPayload? = null) =
        send(WebSocketData(opcode.id, gson.toJsonTree(payload)))

    /**
     * Request for the web socket to be closed
     *
     * @param reason The reason for closing
     *
     * @author Koding
     * @since 0.1-PRE
     */
    suspend fun close(reason: CloseReason) {
        // Run the close function
        close.invoke(reason)

        // Post to the event bus
        dislin.eventBus.post(WebSocketClosedEvent(reason.code.toInt(), reason.message))

        // Cleanup
        listener.cleanup(this)
    }

}
