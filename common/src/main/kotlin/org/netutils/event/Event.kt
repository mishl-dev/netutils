package org.netutils.event

/**
 * Base class for all events in the NetUtils event system.
 * Events can be cancelled to prevent further processing.
 */
open class Event {
    /**
     * Whether this event has been cancelled.
     * Cancelled events may be ignored by listeners.
     */
    var cancelled: Boolean = false
        private set
    
    /**
     * Cancel this event to prevent further processing.
     */
    fun cancel() {
        cancelled = true
    }
}
