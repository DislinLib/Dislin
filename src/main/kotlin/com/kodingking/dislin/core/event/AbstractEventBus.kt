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

package com.kodingking.dislin.core.event

import kotlin.reflect.KClass

/**
 * Inheritable class for all different handlers
 * of events across Dislin to subclass
 *
 * @author Koding
 * @since 0.1-PRE
 */
abstract class AbstractEventBus {

    /**
     * Post an event to the event bus
     *
     * @param event The event to post
     *
     * @author Koding
     * @since 0.1-PRE
     */
    abstract fun post(event: Event): Unit?

    /**
     * Listen for an event on the bus
     *
     * @param clazz    The event class
     * @param listener The listener for the event
     *
     * @author Koding
     * @since 0.1-PRE
     */
    abstract fun <T : Event> listen(clazz: KClass<T>, priority: Int = 0, listener: (T) -> Unit)

    /**
     * Listen for an event on the bus
     *
     * @param listener The event listener
     *
     * @author Koding
     * @since 0.1-PRE
     */
    inline fun <reified T : Event> listen(priority: Int = 0, noinline listener: (T) -> Unit) =
        // Listen for the event
        listen(T::class, priority, listener)

}
