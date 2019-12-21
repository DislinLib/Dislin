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

package com.kodingking.dislin.core.event.basic

import com.kodingking.dislin.core.event.AbstractEventBus
import com.kodingking.dislin.core.event.Event
import kotlin.reflect.KClass

/**
 * Basic event bus, which handles events
 * either synchronously or async
 *
 * @author Koding
 * @since 0.1-PRE
 */
class BasicEventBus : AbstractEventBus() {

    /**
     * Map of registered event listeners
     */
    private val listeners = hashMapOf<KClass<out Event>, Array<Pair<Int, (Event) -> Unit>>>()

    /**
     * Post an event to the bus
     *
     * @param event The event to post
     *
     * @author Koding
     * @since 0.1-PRE
     */
    override fun post(event: Event) =
        // Get the listeners
        listeners[event::class]?.forEach { (_, e) ->
            // Execute the listener
            e.invoke(event)
        }

    /**
     * Register an event listener to the bus
     *
     * @param clazz    The listener's event class
     * @param listener The listener to register
     *
     * @author Koding
     * @since 0.1-PRE
     */
    override fun <T : Event> listen(clazz: KClass<T>, priority: Int, listener: (T) -> Unit) {
        // Get the listeners
        var items = listeners[clazz] ?: arrayOf()

        // Add the listener
        @Suppress("UNCHECKED_CAST")
        items += priority to listener as (Event) -> Unit

        // Sort the listeners
        items.sortByDescending { (priority, _) ->
            // By the priority
            priority
        }

        // Redefine the listeners
        listeners[clazz] = items
    }
}
