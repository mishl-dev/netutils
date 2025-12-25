package org.netutils.event.events

import net.minecraft.client.gui.screens.Screen
import org.netutils.event.Event

/**
 * Event fired when a screen is opened or closed.
 * 
 * @param screen The screen that was opened, or null if closed
 */
class OpenScreenEvent(val screen: Screen?) : Event()
