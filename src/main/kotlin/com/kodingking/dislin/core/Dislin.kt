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

package com.kodingking.dislin.core

import com.kodingking.dislin.core.event.basic.BasicEventBus
import com.kodingking.dislin.io.websocket.DiscordWebSocket
import io.ktor.http.cio.websocket.CloseReason
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Main Dislin class which manages all operations
 * with Discord communication
 *
 * @author Koding
 * @since 0.1-PRE
 */
class Dislin(
    token: String,
    private var autoReconnect: Boolean = true
) {

    /**
     * The current Web Socket connection
     */
    private var webSocket = DiscordWebSocket(token, this)

    /**
     * The core coroutine scope
     */
    private val scope = CoroutineScope(CoroutineName("Dislin"))

    /**
     * The event bus
     */
    val eventBus = BasicEventBus()

    /**
     * Start the connection
     *
     * @author Koding
     * @since 0.1-PRE
     */
    fun start() {
        // Start the websocket
        startWebSocket()
    }

    /**
     * Disconnect from Discord
     *
     * @author Koding
     * @since 0.1-PRE
     */
    suspend fun disconnect() {
        // Disable auto reconnect
        autoReconnect = false

        // Disconnect
        webSocket.close(CloseReason(1000, "Disconnecting"))
    }

    private fun startWebSocket(): Job =
        scope.launch {
            // Connect to the socket and block
            webSocket.connect()

            // Check if we should reconnect
            if (autoReconnect) {
                // Start the socket
                startWebSocket()
            }
        }

}
