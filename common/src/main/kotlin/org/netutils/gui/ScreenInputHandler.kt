package org.netutils.gui

import dev.architectury.event.events.client.ClientScreenInputEvent
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import org.netutils.SharedVariables

/**
 * Handles screen input events for NetUtils EditBox functionality.
 * This is needed because AbstractContainerScreen intercepts key events
 * before they reach the EditBox widget.
 */
object ScreenInputHandler {
    
    /**
     * Reference to the active chat field in container screens.
     * Set by HandledScreenMixin when the screen is initialized.
     */
    @JvmField
    var activeContainerChatField: EditBox? = null
    
    fun register() {
        // Handle key presses before the screen processes them
        ClientScreenInputEvent.KEY_PRESSED_PRE.register { client, screen, keyEvent ->
            if (!SharedVariables.enabled) return@register dev.architectury.event.EventResult.pass()
            
            // Only handle for container screens with our chat field
            if (screen is AbstractContainerScreen<*> && activeContainerChatField != null) {
                val chatField = activeContainerChatField!!
                
                // If the chat field is focused, we handle ALL input to prevent double processing
                if (chatField.isFocused) {
                    val keyCode = keyEvent.key() 

                    // Handle Enter key (257) and Numpad Enter (335)
                    if (keyCode == 257 || keyCode == 335) {
                        val text = chatField.value
                        if (text.isNotEmpty()) {
                            val mc = Minecraft.getInstance()
                            
                            // Handle client commands
                            if (text.startsWith(org.netutils.command.ClientCommands.PREFIX)) {
                                org.netutils.command.ClientCommands.tryExecute(text)
                                chatField.value = ""
                                return@register dev.architectury.event.EventResult.interruptTrue()
                            }
                            
                            // Send chat/command
                            if (mc.connection != null) {
                                if (text.startsWith("/")) {
                                    mc.connection!!.sendCommand(text.substring(1))
                                } else {
                                    mc.connection!!.sendChat(text)
                                }
                            }
                            chatField.value = ""
                        }
                        return@register dev.architectury.event.EventResult.interruptTrue()
                    }

                    // Handle Escape (256) to unfocus
                    if (keyCode == 256) {
                        chatField.isFocused = false
                        return@register dev.architectury.event.EventResult.interruptTrue()
                    }
                    
                    // Forward key event to EditBox and ALWAYS interrupt to prevent double input
                    chatField.keyPressed(keyEvent)
                    return@register dev.architectury.event.EventResult.interruptTrue()
                }
            }
            
            dev.architectury.event.EventResult.pass()
        }
        
        // Char typed events are handled by vanilla propagation
        // We only intercept KEY_PRESSED to prevent screen closing on 'E'
    }
}
