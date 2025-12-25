package org.netutils.util

import net.minecraft.client.gui.screens.Screen
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Tracks screen navigation history.
 * Useful for implementing "back" navigation or screen restoration.
 */
object ScreenHistory {
    
    private val history = ConcurrentLinkedDeque<Screen?>()
    private const val MAX_HISTORY_SIZE = 20
    
    /**
     * Push a screen onto the history stack.
     */
    fun push(screen: Screen?) {
        history.addFirst(screen)
        
        // Limit history size
        while (history.size > MAX_HISTORY_SIZE) {
            history.removeLast()
        }
    }
    
    /**
     * Pop the most recent screen from history.
     * @return The most recent screen, or null if history is empty
     */
    fun pop(): Screen? {
        return history.pollFirst()
    }
    
    /**
     * Peek at the most recent screen without removing it.
     * @return The most recent screen, or null if history is empty
     */
    fun peek(): Screen? {
        return history.peekFirst()
    }
    
    /**
     * Get the previous screen (second in history).
     * Useful for "go back" functionality.
     */
    fun previous(): Screen? {
        val iter = history.iterator()
        if (iter.hasNext()) iter.next() // skip current
        return if (iter.hasNext()) iter.next() else null
    }
    
    /**
     * Clear all history.
     */
    fun clear() {
        history.clear()
    }
    
    /**
     * Get the number of screens in history.
     */
    fun size(): Int = history.size
}
