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

package com.kodingking.dislin.io.websocket.listener

import com.google.gson.JsonElement
import com.kodingking.dislin.io.websocket.DiscordWebSocket
import com.kodingking.dislin.io.websocket.entity.*
import com.kodingking.dislin.util.content.readAs
import io.ktor.http.cio.websocket.CloseReason
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import kotlin.random.Random

/**
 * The default web socket listener for
 * handling events from Discord
 *
 * @author Koding
 * @since 0.1-PRE
 */
class DefaultWebSocketListener(
    private val token: String
) : IWebSocketListener {

    /**
     * The heartbeat interval
     */
    private var heartbeatInterval = -1L

    /**
     * The last timestamp since we
     * received a heartbeat acknowledgement
     */
    private var lastHeartbeat = -1L

    /**
     * The session ID
     */
    var sessionId: String? = null

    /**
     * The current sequence number
     */
    var sequence: Int? = null

    /**
     * The coroutine scope
     */
    private val scope = CoroutineScope(CoroutineName("WebSocket Listener"))

    /**
     * The logger
     */
    private val logger = LogManager.getLogger("Discord-WebSocket")

    /**
     * The listener
     *
     * @author Koding
     * @since 0.1-PRE
     */
    override suspend fun onReceived(socket: DiscordWebSocket, opcode: EnumWebSocketOpcode, data: WebSocketData) {
        // Set the sequence number
        sequence = data.sequence ?: sequence

        // Check the opcode type
        when (opcode) {
            // If it's a hello packet
            EnumWebSocketOpcode.HELLO -> {
                // Read the payload
                val payload = data.content<HelloPayload>()

                // Define the heartbeat interval
                heartbeatInterval = payload.heartbeatInterval

                // Reset last heartbeat
                lastHeartbeat = -1L

                // Schedule the heartbeat task
                startHeartBeating(socket)

                // Check if the session ID exists
                if (sessionId != null && sessionId != null) {
                    // Attempt to resume
                    socket.send(
                        EnumWebSocketOpcode.RESUME,
                        ResumePayload(token, sessionId!!, sequence!!)
                    )
                } else {
                    // Attempt to connect
                    socket.send(
                        EnumWebSocketOpcode.IDENTIFY,
                        IdentifyPayload(
                            token,
                            IdentifyConnectionProperties(
                                System.getProperty("os.name"),
                                "Dislin",
                                "Dislin"
                            )
                        )
                    )
                }
            }

            // If it's a heartbeat acknowledgement packet
            EnumWebSocketOpcode.HEARTBEAT_ACK -> {
                // Debug log
                logger.debug("Heartbeat has been acknowledged")

                // Set the timestamp
                lastHeartbeat = System.currentTimeMillis()
            }

            // If it's a dispatch event
            EnumWebSocketOpcode.DISPATCH -> {
                // Get the event string
                val eventString = data.event ?: return

                // Get the event type
                val event = EnumGatewayEvent.values().firstOrNull {
                    // Check if the name equals
                    it.name.equals(eventString, true)
                } ?: return

                // Hand the event
                onEvent(socket, event, data.data)
            }

            // Listen for an invalid session
            EnumWebSocketOpcode.INVALID_SESSION -> {
                // Check if we can resume
                val resume = data.data.asBoolean

                // If we can't resume
                if (!resume) {
                    // Clear the sequence & session ID
                    sequence = null
                    sessionId = null
                }

                // Wait a random amount of time
                delay(Random.nextLong(1000, 5000))

                // Close the session
                socket.close(CloseReason(4000, "Invalid session"))
            }

            // Otherwise ignore
            else -> {
            }
        }
    }

    /**
     * Cleanup the listener for it to be reused
     *
     * @param socket The Discord socket
     *
     * @author Koding
     * @since 0.1-PRE
     */
    override suspend fun cleanup(socket: DiscordWebSocket) {
        // Cancel the tasks
        scope.cancel()
    }

    /**
     * Fired on any gateway event
     *
     * @param socket The connection to Discord
     * @param event  The event type
     * @param data   The event data
     *
     * @author Koding
     * @since 0.1-PRE
     */
    private fun onEvent(socket: DiscordWebSocket, event: EnumGatewayEvent, data: JsonElement) {
        // Check the event type
        when (event) {
            // Check for a ready event
            EnumGatewayEvent.READY -> {
                // Read the payload
                val payload = data.readAs<ReadyEventPayload>()

                // Store the session id
                sessionId = payload.sessionId
            }

            // Otherwise do nothing
            else -> {}
        }
    }

    /**
     * Start the heart beat task
     *
     * @param socket The web socket to send the data to
     *
     * @author Koding
     * @since 0.1-PRE
     */
    private fun startHeartBeating(socket: DiscordWebSocket) = scope.launch {
        // Run forever
        while (true) {
            // Send a heartbeat
            socket.send(EnumWebSocketOpcode.HEARTBEAT)

            // Check if the connection has failed
            // or is zombied
            if (lastHeartbeat > 0 && lastHeartbeat + heartbeatInterval < System.currentTimeMillis()) {
                // Log
                logger.error("Zombied connection detected.")

                // Close the web socket
                socket.close(CloseReason(4000, "Failed connection"))

                // Return
                return@launch
            }

            // Log
            logger.debug("Sending heartbeat to Discord")

            // Wait for another heartbeat
            delay(heartbeatInterval)
        }
    }
}
