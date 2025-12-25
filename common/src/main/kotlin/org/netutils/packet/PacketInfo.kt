package org.netutils.packet

import net.minecraft.network.protocol.Packet
import java.lang.reflect.Constructor

/**
 * Metadata about a packet type for the packet fabrication system.
 * 
 * @param key Unique identifier for this packet (e.g., "play.hand_swing")
 * @param packetClass The packet class
 * @param constructor The constructor to use for creating instances
 * @param usageString Human-readable usage description
 */
data class PacketInfo(
    val key: String,
    val packetClass: Class<out Packet<*>>,
    val constructor: Constructor<*>,
    val usageString: String
)
