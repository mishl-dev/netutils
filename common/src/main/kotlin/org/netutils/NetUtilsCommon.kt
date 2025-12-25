package org.netutils

import dev.architectury.event.events.client.ClientTickEvent
import dev.architectury.registry.client.keymappings.KeyMappingRegistry
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import com.mojang.blaze3d.platform.InputConstants
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory
import org.netutils.screen.ScreenSaver

/**
 * NetUtils - Common
 * Cross-platform initialization code shared between Fabric and NeoForge.
 */
object NetUtilsCommon {
    const val MOD_ID = "netutils"
    @JvmField val LOGGER = LoggerFactory.getLogger(MOD_ID)!!
    val mc: Minecraft get() = Minecraft.getInstance()
    
    const val KEYBIND_CATEGORY = "key.categories.netutils"
    
    lateinit var restoreScreenKey: KeyMapping
    
    fun init() {
        LOGGER.info("Initializing NetUtils")
        
        // Register screen input handler for EditBox functionality
        org.netutils.gui.ScreenInputHandler.register()
        
        // Register client-side commands
        org.netutils.command.ClientCommands.register()
        
        // Register keybindings using Architectury API
        // Note: Custom category names require platform-specific registration
        // Using MISC for cross-platform compatibility
        restoreScreenKey = KeyMapping(
            "key.netutils.restore_screen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KeyMapping.Category.MISC
        )
        KeyMappingRegistry.register(restoreScreenKey)
        
        // Tick event for keybind handling
        ClientTickEvent.CLIENT_POST.register { _ ->
            while (restoreScreenKey.consumeClick()) {
                if (mc.player != null) {
                    try {
                        val info = ScreenSaver.getInfo("default")
                        if (info == "Empty slot") {
                            mc.player?.displayClientMessage(
                                Component.literal("§c[NetUtils] No screen saved. Use Save GUI button first."),
                                false
                            )
                        } else {
                            ScreenSaver.loadScreen("default")
                            mc.player?.displayClientMessage(
                                Component.literal("§a[NetUtils] Restored saved screen."),
                                true
                            )
                        }
                    } catch (e: Exception) {
                        mc.player?.displayClientMessage(
                            Component.literal("§c[NetUtils] ${e.message}"),
                            false
                        )
                    }
                }
            }
        }
        
        LOGGER.info("NetUtils initialized successfully")
    }
}
