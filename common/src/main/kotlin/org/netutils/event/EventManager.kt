package org.netutils.event

import org.netutils.NetUtilsCommon
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A simple reflection-based event bus for NetUtils.
 * Supports registering listeners with @Subscribe annotated methods.
 */
object EventManager {
    
    // Map of event type -> list of handler methods
    private val listeners = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<Method>>()
    
    // Map of listener instance -> list of its handler methods
    private val listenerInstances = ConcurrentHashMap<Any, List<Method>>()
    
    /**
     * Register an event listener.
     * The listener's methods annotated with @Subscribe will be called when matching events are triggered.
     * 
     * @param listener The listener instance, or a Class to instantiate
     */
    fun addListener(listener: Any) {
        val instance = if (listener is Class<*>) {
            try {
                listener.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                NetUtilsCommon.LOGGER.error("Failed to instantiate listener: ${listener.name}", e)
                return
            }
        } else {
            listener
        }
        
        val methods = mutableListOf<Method>()
        
        for (method in instance.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(Subscribe::class.java) &&
                method.parameterCount == 1 &&
                Event::class.java.isAssignableFrom(method.parameterTypes[0])) {
                
                method.isAccessible = true
                
                @Suppress("UNCHECKED_CAST")
                val eventType = method.parameterTypes[0] as Class<out Event>
                listeners.computeIfAbsent(eventType) { CopyOnWriteArrayList() }.add(method)
                methods.add(method)
            }
        }
        
        listenerInstances[instance] = methods
    }
    
    /**
     * Remove an event listener.
     * 
     * @param listener The listener instance or Class to remove
     */
    fun removeListener(listener: Any) {
        if (listener is Class<*>) {
            listenerInstances.entries.removeIf { it.key.javaClass == listener }
        } else {
            val methods = listenerInstances.remove(listener) ?: return
            for (method in methods) {
                @Suppress("UNCHECKED_CAST")
                val eventType = method.parameterTypes[0] as Class<out Event>
                listeners[eventType]?.remove(method)
            }
        }
    }
    
    /**
     * Trigger an event, calling all registered handlers.
     * 
     * @param event The event to trigger
     */
    fun <T : Event> trigger(event: T) {
        val eventListeners = listeners[event.javaClass] ?: return
        
        for (method in eventListeners) {
            try {
                // Find the listener instance that owns this method
                for ((instance, methods) in listenerInstances) {
                    if (method in methods && method.declaringClass.isInstance(instance)) {
                        method.invoke(instance, event)
                        break
                    }
                }
            } catch (e: Exception) {
                NetUtilsCommon.LOGGER.error("Error invoking event handler: ${method.name}", e)
            }
            
            // Stop processing if event was cancelled
            if (event.cancelled) {
                break
            }
        }
    }
}
