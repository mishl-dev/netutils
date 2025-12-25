package org.netutils.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import org.netutils.SharedVariables
import org.netutils.NetUtilsCommon
import org.netutils.mixin.accessor.ScreenAccessor
import org.netutils.screen.ScreenSaver
import org.netutils.util.SlotManager
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen

object WidgetUtils {
    fun createWidgets(mc: Minecraft, screen: Screen) {
        val accessor = screen as ScreenAccessor
        
        // Row 1: Delay (Left) | Block (Right)
        // Delay (5, 5) w=79
        accessor.invokeAddRenderableWidget(Button.builder(Component.literal(if (SharedVariables.delayUIPackets) "Queue: ON" else "Queue: OFF")) { btn ->
            SharedVariables.delayUIPackets = !SharedVariables.delayUIPackets
            btn.message = Component.literal(if (SharedVariables.delayUIPackets) "Queue: ON" else "Queue: OFF")
            
            if (!SharedVariables.delayUIPackets && SharedVariables.delayedUIPackets.isNotEmpty() && mc.connection != null) {
                val size = SharedVariables.delayedUIPackets.size
                val connectionAccessor = mc.connection!!.connection as org.netutils.mixin.accessor.ClientConnectionAccessor
                for (packet in SharedVariables.delayedUIPackets) {
                     @Suppress("UNCHECKED_CAST")
                     mc.connection!!.send(packet as net.minecraft.network.protocol.Packet<*>)
                     // Also send directly to channel to ensure immediate transmission
                     connectionAccessor.channel.writeAndFlush(packet)
                }
                mc.player?.displayClientMessage(Component.literal("§a[NetUtils] Sent $size delayed packets."), false)
                SharedVariables.delayedUIPackets.clear()
            } else if (SharedVariables.delayUIPackets) {
                mc.player?.displayClientMessage(Component.literal("§e[NetUtils] Packet queueing enabled."), false)
            }
        }.bounds(5, 5, 79, 20).build())

        // Send -> Block (86, 5) w=79
        accessor.invokeAddRenderableWidget(Button.builder(Component.literal(if (SharedVariables.sendUIPackets) "Block: OFF" else "Block: ON")) { btn ->
            SharedVariables.sendUIPackets = !SharedVariables.sendUIPackets
            btn.message = Component.literal(if (SharedVariables.sendUIPackets) "Block: OFF" else "Block: ON")
            val status = if (SharedVariables.sendUIPackets) "disabled" else "enabled"
            mc.player?.displayClientMessage(Component.literal("§e[NetUtils] Packet blocking $status."), false)
        }.bounds(86, 5, 79, 20).build())
        
        // Row 2: Close without packet (5, 27) w=160
        accessor.invokeAddRenderableWidget(Button.builder(Component.literal("Close without packet")) {
            val player = mc.player
            val wasSleeping = player?.isSleeping == true
            val previousBlockingState = SharedVariables.sendUIPackets
            
            // If sleeping, force blocking on to catch the stop_sleeping packet
            if (wasSleeping) {
                SharedVariables.sendUIPackets = false
                player?.stopSleeping() // Force wake up locally
            }

            if (screen is AbstractSignEditScreen) {
                SharedVariables.shouldEditSign = false
            }
            
            mc.setScreen(null)
            
            // Restore blocking state if we changed it
            if (wasSleeping) {
                SharedVariables.sendUIPackets = previousBlockingState
                player?.displayClientMessage(Component.literal("§a[NetUtils] Closed bed without packet."), true)
            }
        }.bounds(5, 27, 160, 20).build())
        
        // Row 3: De-sync (5, 49) w=160
        accessor.invokeAddRenderableWidget(Button.builder(Component.literal("De-sync")) {
            if (mc.connection != null && mc.player != null) {
                mc.connection!!.send(ServerboundContainerClosePacket(mc.player!!.containerMenu.containerId))
            } else {
                NetUtilsCommon.LOGGER.warn("Network handler or player was null during De-sync")
            }
        }.bounds(5, 49, 160, 20).build())
        
        // Row 4: Disconnect and send packets (5, 71) w=160
        accessor.invokeAddRenderableWidget(Button.builder(Component.literal("Disconnect & Send")) {
            SharedVariables.delayUIPackets = false
             if (mc.connection != null) {
                val connectionAccessor = mc.connection!!.connection as org.netutils.mixin.accessor.ClientConnectionAccessor
                for (packet in SharedVariables.delayedUIPackets) {
                     @Suppress("UNCHECKED_CAST")
                     mc.connection!!.send(packet as net.minecraft.network.protocol.Packet<*>)
                     // Also send directly to channel to ensure immediate transmission
                     connectionAccessor.channel.writeAndFlush(packet)
                }
                mc.connection!!.connection.disconnect(Component.literal("Disconnecting (NetUtils)"))
            }
            SharedVariables.delayedUIPackets.clear()
        }.bounds(5, 71, 160, 20).build())

        // Row 5: Save GUI | Copy Title
        // Save GUI (5, 93) w=79
        accessor.invokeAddRenderableWidget(Button.builder(Component.literal("Save GUI")) {
             try {
                ScreenSaver.saveScreen("default")
                mc.player?.displayClientMessage(Component.literal("§a[NetUtils] GUI Saved."), false)
             } catch (e: Exception) {
                mc.player?.displayClientMessage(Component.literal("§c[NetUtils] ${e.message}"), false)
             }
        }.bounds(5, 93, 79, 20).build())
        
        // Copy Title (86, 93) w=79
        accessor.invokeAddRenderableWidget(Button.builder(Component.literal("Copy Title")) {
             try {
                if (mc.screen != null) {
                    val title = mc.screen!!.title.string
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(title), null)
                    mc.player?.displayClientMessage(Component.literal("§a[NetUtils] Title Copied."), false)
                }
             } catch (e: Exception) {
                 NetUtilsCommon.LOGGER.error("Error copying title", e)
             }
        }.bounds(86, 93, 79, 20).build())
        
        // Row 6: Show Slot IDs | Pick Slot
        // Show Slot IDs (5, 115) w=79
        accessor.invokeAddRenderableWidget(Button.builder(Component.literal(if (SlotManager.shouldDrawSlotIDs) "IDs: ON" else "IDs: OFF")) { btn ->
            SlotManager.toggleSlotIDs()
            btn.message = Component.literal(if (SlotManager.shouldDrawSlotIDs) "IDs: ON" else "IDs: OFF")
        }.bounds(5, 115, 160, 20).build())
        

        

        
        // Row 8: Help - Show commands (5, 137) w=160
        accessor.invokeAddRenderableWidget(Button.builder(Component.literal("Help (^help)")) {
            mc.player?.displayClientMessage(Component.literal("""§e[NetUtils] Commands (^help for full list):
§7^menuinfo - Show GUI info
§7^trades - List villager trades
§7^trade <id> - Select trade by ID
§7^packets / ^rawsend"""), false)
        }.bounds(5, 137, 160, 20).build())
    }
    
    private fun getBoolStr(b: Boolean): String = if (b) "ON" else "OFF"
}
