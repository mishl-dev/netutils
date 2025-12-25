package org.netutils.screen

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import org.netutils.NetUtilsCommon
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages saving and loading screen states.
 * Allows players to save their current screen and restore it later.
 */
object ScreenSaver {
    
    private val savedScreens = ConcurrentHashMap<String, SavedScreen>()
    
    init {
        // Initialize with a "default" slot
        savedScreens["default"] = SavedScreen(null, null)
    }
    
    /**
     * Save the current screen to a named slot.
     * 
     * @param name The slot name to save to
     * @throws IllegalStateException if no screen is currently open
     */
    fun saveScreen(name: String) {
        val mc = Minecraft.getInstance()
        val currentScreen = mc.screen
            ?: throw IllegalStateException("Cannot save when no screen is open.")
        
        val menu = if (currentScreen is AbstractContainerScreen<*>) {
            mc.player?.containerMenu
        } else {
            null
        }
        
        savedScreens[name] = SavedScreen(currentScreen, menu)
        NetUtilsCommon.LOGGER.info("Saved screen '${currentScreen.javaClass.simpleName}' to slot '$name'")
    }
    
    /**
     * Load a saved screen from a named slot.
     * 
     * @param name The slot name to load from
     * @throws IllegalArgumentException if no screen is saved in that slot
     */
    fun loadScreen(name: String) {
        val saved = savedScreens[name]
            ?: throw IllegalArgumentException("No screen saved in slot: \"$name\"")
        
        val mc = Minecraft.getInstance()
        
        mc.execute {
            mc.setScreen(saved.screen)
            
            // Restore the container menu if available
            if (mc.player != null && saved.menu != null) {
                mc.player!!.containerMenu = saved.menu
            }
        }
        
        NetUtilsCommon.LOGGER.info("Loaded screen from slot '$name'")
    }
    
    /**
     * Get info about a saved screen.
     * 
     * @param name The slot name to query
     * @return Info string, or null if not found
     */
    fun getInfo(name: String): String? {
        val saved = savedScreens[name] ?: return null
        
        return if (saved.screen != null) {
            val className = saved.screen.javaClass.simpleName
            val title = saved.screen.title.string
            "Class: $className, Title: $title"
        } else {
            "Empty slot"
        }
    }
    
    /**
     * Remove a saved screen.
     * 
     * @param name The slot name to remove
     * @return true if removed, false if not found
     */
    fun removeScreen(name: String): Boolean {
        return savedScreens.remove(name) != null
    }
    
    /**
     * List all saved screen slot names.
     */
    fun listSlots(): Set<String> = savedScreens.keys.toSet()
    
    /**
     * Check if a slot exists.
     */
    fun hasSlot(name: String): Boolean = savedScreens.containsKey(name)
}
