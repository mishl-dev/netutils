package org.netutils.screen

import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.inventory.AbstractContainerMenu

/**
 * Represents a saved screen state including the screen and its container menu.
 * 
 * @param screen The saved screen instance
 * @param menu The container menu associated with the screen (if any)
 */
data class SavedScreen(
    val screen: Screen?,
    val menu: AbstractContainerMenu?
)
