package org.netutils

import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.protocol.Packet
import net.minecraft.world.inventory.AbstractContainerMenu

/**
 * Shared state variables for NetUtils.
 * No sensitive data stored here.
 */
object SharedVariables {
    // Packet handling
    @JvmField var sendUIPackets = true
    @JvmField var delayUIPackets = false
    @JvmField val delayedUIPackets = mutableListOf<Packet<*>>()
    
    // Screen restore
    @JvmField var storedScreen: Screen? = null
    @JvmField var storedScreenHandler: AbstractContainerMenu? = null
    
    // Feature toggles
    @JvmField var enabled = true
    @JvmField var bypassResourcePack = false
    @JvmField var resourcePackForceDeny = false
    @JvmField var allowKeepAlive = true
    
    // Platform detection
    @JvmField val isMac = System.getProperty("os.name").lowercase().contains("mac")
    @JvmField var shouldEditSign = true
}

