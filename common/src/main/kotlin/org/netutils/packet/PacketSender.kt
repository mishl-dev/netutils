package org.netutils.packet

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
import net.minecraft.network.protocol.game.*
import net.minecraft.world.InteractionHand
import org.netutils.NetUtilsCommon
import java.util.*

/**
 * Utility for sending fabricated packets.
 * Parses command-line style arguments and constructs packets dynamically.
 */
object PacketSender {
    
    private val mc = Minecraft.getInstance()
    
    /**
     * Send a packet by its key with the given arguments.
     * 
     * @param packetKey The packet key (e.g., "play.hand_swing")
     * @param times Number of times to send the packet
     * @param args Arguments for packet construction
     * @return Result message
     */
    fun sendPacket(packetKey: String, times: Int, args: List<String>): String {
        if (mc.connection == null) {
            return "§c[NetUtils] Not connected to a server."
        }
        
        if (times <= 0) {
            return "§c[NetUtils] Times must be > 0"
        }
        
        return try {
            val packet = createPacket(packetKey, args)
                ?: return "§c[NetUtils] Unknown packet: $packetKey"
            
            repeat(times) {
                mc.connection!!.send(packet)
            }
            
            "§a[NetUtils] Sent '$packetKey' x$times"
        } catch (e: Exception) {
            NetUtilsCommon.LOGGER.error("Failed to send packet: $packetKey", e)
            "§c[NetUtils] Error: ${e.message}"
        }
    }
    
    /**
     * Create a packet from the given key and arguments.
     */
    private fun createPacket(packetKey: String, args: List<String>): net.minecraft.network.protocol.Packet<*>? {
        return when (packetKey) {
            "play.hand_swing" -> {
                val hand = PacketRegistry.parseHand(args.getOrNull(0) ?: "main_hand")
                ServerboundSwingPacket(hand)
            }
            
            "play.close_handled_screen" -> {
                val syncId = args.getOrNull(0)?.toIntOrNull() 
                    ?: mc.player?.containerMenu?.containerId 
                    ?: 0
                ServerboundContainerClosePacket(syncId)
            }
            
            "play.player_move.position" -> {
                val x = args.getOrNull(0)?.toDoubleOrNull() ?: mc.player?.x ?: 0.0
                val y = args.getOrNull(1)?.toDoubleOrNull() ?: mc.player?.y ?: 0.0
                val z = args.getOrNull(2)?.toDoubleOrNull() ?: mc.player?.z ?: 0.0
                val onGround = args.getOrNull(3)?.toBooleanStrictOrNull() ?: true
                val horizontalCollision = args.getOrNull(4)?.toBooleanStrictOrNull() ?: false
                ServerboundMovePlayerPacket.Pos(x, y, z, onGround, horizontalCollision)
            }
            
            "play.player_move.rot" -> {
                val yaw = args.getOrNull(0)?.toFloatOrNull() ?: mc.player?.yRot ?: 0f
                val pitch = args.getOrNull(1)?.toFloatOrNull() ?: mc.player?.xRot ?: 0f
                val onGround = args.getOrNull(2)?.toBooleanStrictOrNull() ?: true
                val horizontalCollision = args.getOrNull(3)?.toBooleanStrictOrNull() ?: false
                ServerboundMovePlayerPacket.Rot(yaw, pitch, onGround, horizontalCollision)
            }
            
            "play.player_move.full" -> {
                val x = args.getOrNull(0)?.toDoubleOrNull() ?: mc.player?.x ?: 0.0
                val y = args.getOrNull(1)?.toDoubleOrNull() ?: mc.player?.y ?: 0.0
                val z = args.getOrNull(2)?.toDoubleOrNull() ?: mc.player?.z ?: 0.0
                val yaw = args.getOrNull(3)?.toFloatOrNull() ?: mc.player?.yRot ?: 0f
                val pitch = args.getOrNull(4)?.toFloatOrNull() ?: mc.player?.xRot ?: 0f
                val onGround = args.getOrNull(5)?.toBooleanStrictOrNull() ?: true
                val horizontalCollision = args.getOrNull(6)?.toBooleanStrictOrNull() ?: false
                ServerboundMovePlayerPacket.PosRot(x, y, z, yaw, pitch, onGround, horizontalCollision)
            }
            
            "play.teleport_confirm" -> {
                val teleportId = args.getOrNull(0)?.toIntOrNull() ?: 0
                ServerboundAcceptTeleportationPacket(teleportId)
            }
            
            "common.keep_alive" -> {
                val id = args.getOrNull(0)?.toLongOrNull() ?: System.currentTimeMillis()
                ServerboundKeepAlivePacket(id)
            }
            
            "play.chat_command" -> {
                val command = args.joinToString(" ")
                ServerboundChatCommandPacket(command)
            }
            
            "play.button_click" -> {
                val containerId = args.getOrNull(0)?.toIntOrNull() 
                    ?: mc.player?.containerMenu?.containerId 
                    ?: 0
                val buttonId = args.getOrNull(1)?.toIntOrNull() ?: 0
                ServerboundContainerButtonClickPacket(containerId, buttonId)
            }
            
            "play.select_trade" -> {
                val tradeId = args.getOrNull(0)?.toIntOrNull() ?: 0
                ServerboundSelectTradePacket(tradeId)
            }
            
            "play.rename_item" -> {
                val name = args.joinToString(" ")
                ServerboundRenameItemPacket(name)
            }
            
            "play.update_selected_slot" -> {
                val slot = args.getOrNull(0)?.toIntOrNull() ?: 0
                ServerboundSetCarriedItemPacket(slot)
            }
            
            "play.spectator_teleport" -> {
                val uuid = try {
                    UUID.fromString(args.getOrNull(0) ?: "")
                } catch (e: Exception) {
                    return null
                }
                ServerboundTeleportToEntityPacket(uuid)
            }
            
            "play.query_block_nbt" -> {
                val transactionId = args.getOrNull(0)?.toIntOrNull() ?: 0
                val x = args.getOrNull(1)?.toIntOrNull() ?: 0
                val y = args.getOrNull(2)?.toIntOrNull() ?: 0
                val z = args.getOrNull(3)?.toIntOrNull() ?: 0
                ServerboundBlockEntityTagQueryPacket(transactionId, BlockPos(x, y, z))
            }
            
            "play.query_entity_nbt" -> {
                val transactionId = args.getOrNull(0)?.toIntOrNull() ?: 0
                val entityId = args.getOrNull(1)?.toIntOrNull() ?: 0
                ServerboundEntityTagQueryPacket(transactionId, entityId)
            }
            
            else -> null
        }
    }
    
    /**
     * Get usage help for all packets.
     */
    fun getHelp(): String {
        val sb = StringBuilder("§e[NetUtils] Available packets:\n")
        PacketRegistry.getAllPacketKeys().forEach { key ->
            val info = PacketRegistry.getPacketInfo(key)
            if (info != null) {
                sb.append("§7  ${info.usageString}\n")
            }
        }
        return sb.toString()
    }
    
    /**
     * Parse a rawsend command string.
     * Format: ^rawsend <times> <packet_key> [args...]
     * 
     * @return Result message
     */
    fun parseAndSend(commandText: String): String {
        val parts = commandText.trim().split(Regex("\\s+"))
        
        if (parts.size < 2) {
            return "§c[NetUtils] Usage: ^rawsend <times> <packet_key> [args...]"
        }
        
        val times = parts[0].toIntOrNull()
            ?: return "§c[NetUtils] Invalid times: ${parts[0]}"
        
        val packetKey = parts[1]
        val args = if (parts.size > 2) parts.subList(2, parts.size) else emptyList()
        
        return sendPacket(packetKey, times, args)
    }
}
