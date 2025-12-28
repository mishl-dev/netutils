package org.netutils.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.netutils.SharedVariables;

import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;

/**
 * Mixin for Connection to control packet sending.
 * Allows toggling/delaying outbound packets.
 * Based on the original UI-Utils implementation but expanded to cover ALL packets.
 */
@Mixin(Connection.class)
public class ClientConnectionMixin {
    
    /**
     * Check if a packet is vital (keepalive/pong) and should not be blocked/delayed.
     */
    private boolean isVitalPacket(Packet<?> packet) {
        return (packet instanceof ServerboundKeepAlivePacket || packet instanceof ServerboundPongPacket)
                && SharedVariables.allowKeepAlive;
    }
    
    /**
     * Handle packet interception logic for blocking/queueing.
     * @return true if the packet should be cancelled (blocked or queued)
     */
    private boolean handlePacketInterception(Packet<?> packet) {
        boolean isVital = isVitalPacket(packet);

        // Handle "Blocking" mode - Block ALL packets (except vital) if sendUIPackets is false
        if (!SharedVariables.sendUIPackets && !isVital) {
            return true; // Cancel
        }

        // Handle "Delay/Queueing" mode - Queue ALL packets (except vital) if delayUIPackets is true
        if (SharedVariables.delayUIPackets && !isVital) {
            SharedVariables.delayedUIPackets.add(packet);
            return true; // Cancel
        }

        // Special handling for sign editing (bypass)
        if (!SharedVariables.shouldEditSign && packet instanceof ServerboundSignUpdatePacket) {
            SharedVariables.shouldEditSign = true;
            return true; // Cancel
        }

        return false; // Don't cancel, let it through
    }
    
    /**
     * Intercepts packets at the basic send method.
     * This is the primary packet send method in Connection.
     */
    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        if (handlePacketInterception(packet)) {
            ci.cancel();
        }
    }
}

