package org.netutils.packet

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
import net.minecraft.network.protocol.game.*
import net.minecraft.world.InteractionHand
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.phys.Vec3
import org.netutils.NetUtilsCommon
import java.util.*

/**
 * Registry of all supported C2S packets that can be fabricated.
 * Provides packet metadata and construction helpers.
 */
object PacketRegistry {
    
    private val packets = mutableMapOf<String, PacketInfo>()
    
    init {
        registerPackets()
    }
    
    private fun registerPackets() {
        // Hand swing packet
        registerPacket("play.hand_swing", ServerboundSwingPacket::class.java) { args ->
            val hand = parseHand(args.getOrNull(0) ?: "main_hand")
            ServerboundSwingPacket(hand)
        }
        
        // Close container packet
        registerPacket("play.close_handled_screen", ServerboundContainerClosePacket::class.java) { args ->
            val syncId = args.getOrNull(0)?.toIntOrNull() ?: 0
            ServerboundContainerClosePacket(syncId)
        }
        
        // Player position packet
        registerPacket("play.player_move.position", ServerboundMovePlayerPacket.Pos::class.java) { args ->
            val x = args.getOrNull(0)?.toDoubleOrNull() ?: 0.0
            val y = args.getOrNull(1)?.toDoubleOrNull() ?: 0.0
            val z = args.getOrNull(2)?.toDoubleOrNull() ?: 0.0
            val onGround = args.getOrNull(3)?.toBooleanStrictOrNull() ?: true
            val horizontalCollision = args.getOrNull(4)?.toBooleanStrictOrNull() ?: false
            ServerboundMovePlayerPacket.Pos(x, y, z, onGround, horizontalCollision)
        }
        
        // Player look packet
        registerPacket("play.player_move.rot", ServerboundMovePlayerPacket.Rot::class.java) { args ->
            val yaw = args.getOrNull(0)?.toFloatOrNull() ?: 0f
            val pitch = args.getOrNull(1)?.toFloatOrNull() ?: 0f
            val onGround = args.getOrNull(2)?.toBooleanStrictOrNull() ?: true
            val horizontalCollision = args.getOrNull(3)?.toBooleanStrictOrNull() ?: false
            ServerboundMovePlayerPacket.Rot(yaw, pitch, onGround, horizontalCollision)
        }
        
        // Player position and look packet
        registerPacket("play.player_move.full", ServerboundMovePlayerPacket.PosRot::class.java) { args ->
            val x = args.getOrNull(0)?.toDoubleOrNull() ?: 0.0
            val y = args.getOrNull(1)?.toDoubleOrNull() ?: 0.0
            val z = args.getOrNull(2)?.toDoubleOrNull() ?: 0.0
            val yaw = args.getOrNull(3)?.toFloatOrNull() ?: 0f
            val pitch = args.getOrNull(4)?.toFloatOrNull() ?: 0f
            val onGround = args.getOrNull(5)?.toBooleanStrictOrNull() ?: true
            val horizontalCollision = args.getOrNull(6)?.toBooleanStrictOrNull() ?: false
            ServerboundMovePlayerPacket.PosRot(x, y, z, yaw, pitch, onGround, horizontalCollision)
        }
        
        // Teleport confirm packet
        registerPacket("play.teleport_confirm", ServerboundAcceptTeleportationPacket::class.java) { args ->
            val teleportId = args.getOrNull(0)?.toIntOrNull() ?: 0
            ServerboundAcceptTeleportationPacket(teleportId)
        }
        
        // Keep alive packet
        registerPacket("common.keep_alive", ServerboundKeepAlivePacket::class.java) { args ->
            val id = args.getOrNull(0)?.toLongOrNull() ?: 0L
            ServerboundKeepAlivePacket(id)
        }
        
        // Chat message (command execution)
        registerPacket("play.chat_command", ServerboundChatCommandPacket::class.java) { args ->
            val command = args.joinToString(" ")
            ServerboundChatCommandPacket(command)
        }
        
        // Button click packet
        registerPacket("play.button_click", ServerboundContainerButtonClickPacket::class.java) { args ->
            val containerId = args.getOrNull(0)?.toIntOrNull() ?: 0
            val buttonId = args.getOrNull(1)?.toIntOrNull() ?: 0
            ServerboundContainerButtonClickPacket(containerId, buttonId)
        }
        
        // Select trade packet
        registerPacket("play.select_trade", ServerboundSelectTradePacket::class.java) { args ->
            val tradeId = args.getOrNull(0)?.toIntOrNull() ?: 0
            ServerboundSelectTradePacket(tradeId)
        }
        
        // Rename item packet
        registerPacket("play.rename_item", ServerboundRenameItemPacket::class.java) { args ->
            val name = args.joinToString(" ")
            ServerboundRenameItemPacket(name)
        }
        
        // Update selected slot packet
        registerPacket("play.update_selected_slot", ServerboundSetCarriedItemPacket::class.java) { args ->
            val slot = args.getOrNull(0)?.toIntOrNull() ?: 0
            ServerboundSetCarriedItemPacket(slot)
        }
        
        // Spectator teleport packet
        registerPacket("play.spectator_teleport", ServerboundTeleportToEntityPacket::class.java) { args ->
            val uuid = UUID.fromString(args.getOrNull(0) ?: "00000000-0000-0000-0000-000000000000")
            ServerboundTeleportToEntityPacket(uuid)
        }
        
        // Query block NBT packet
        registerPacket("play.query_block_nbt", ServerboundBlockEntityTagQueryPacket::class.java) { args ->
            val transactionId = args.getOrNull(0)?.toIntOrNull() ?: 0
            val x = args.getOrNull(1)?.toIntOrNull() ?: 0
            val y = args.getOrNull(2)?.toIntOrNull() ?: 0
            val z = args.getOrNull(3)?.toIntOrNull() ?: 0
            ServerboundBlockEntityTagQueryPacket(transactionId, BlockPos(x, y, z))
        }
        
        // Query entity NBT packet
        registerPacket("play.query_entity_nbt", ServerboundEntityTagQueryPacket::class.java) { args ->
            val transactionId = args.getOrNull(0)?.toIntOrNull() ?: 0
            val entityId = args.getOrNull(1)?.toIntOrNull() ?: 0
            ServerboundEntityTagQueryPacket(transactionId, entityId)
        }
        
        NetUtilsCommon.LOGGER.info("PacketRegistry initialized with ${packets.size} packets")
    }
    
    private inline fun <reified T : Packet<*>> registerPacket(
        key: String,
        packetClass: Class<T>,
        crossinline factory: (List<String>) -> T
    ) {
        try {
            // Find the first public constructor
            val constructor = packetClass.constructors.firstOrNull()
            if (constructor != null) {
                val usage = buildUsageString(key, constructor)
                packets[key] = PacketInfo(key, packetClass, constructor, usage)
            }
        } catch (e: Exception) {
            NetUtilsCommon.LOGGER.warn("Failed to register packet: $key - ${e.message}")
        }
    }
    
    private fun buildUsageString(key: String, constructor: java.lang.reflect.Constructor<*>): String {
        val params = constructor.parameters.joinToString(" ") { param ->
            "<${param.name}:${param.type.simpleName}>"
        }
        return if (params.isEmpty()) key else "$key $params"
    }
    
    /**
     * Get packet info by key.
     */
    fun getPacketInfo(key: String): PacketInfo? = packets[key]
    
    /**
     * Get all registered packet keys.
     */
    fun getAllPacketKeys(): List<String> = packets.keys.sorted()
    
    /**
     * Parse a hand string to InteractionHand.
     */
    fun parseHand(value: String): InteractionHand {
        return when (value.lowercase()) {
            "off_hand", "offhand", "off" -> InteractionHand.OFF_HAND
            else -> InteractionHand.MAIN_HAND
        }
    }
    
    /**
     * Parse a direction string to Direction.
     */
    fun parseDirection(value: String): Direction {
        return when (value.lowercase()) {
            "up" -> Direction.UP
            "down" -> Direction.DOWN
            "north" -> Direction.NORTH
            "south" -> Direction.SOUTH
            "east" -> Direction.EAST
            "west" -> Direction.WEST
            else -> Direction.UP
        }
    }
    
    /**
     * Parse a click type string to ClickType.
     */
    fun parseClickType(value: String): ClickType {
        return when (value.lowercase()) {
            "pickup" -> ClickType.PICKUP
            "quick_move", "shift" -> ClickType.QUICK_MOVE
            "swap" -> ClickType.SWAP
            "clone", "middle" -> ClickType.CLONE
            "throw" -> ClickType.THROW
            "quick_craft" -> ClickType.QUICK_CRAFT
            "pickup_all" -> ClickType.PICKUP_ALL
            else -> ClickType.PICKUP
        }
    }
}
