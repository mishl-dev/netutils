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
     * Intercepts packets at the basic send method.
     * This catches all packets being sent through the connection.
     */
    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        // Identify vital packets that should never be blocked/delayed (to prevent timeouts)
        boolean isVitalPacket = (packet instanceof ServerboundKeepAlivePacket || packet instanceof ServerboundPongPacket)
                                && SharedVariables.allowKeepAlive;

        // Handle "Blocking" mode - Block ALL packets (except vital) if sendUIPackets is false
        if (!SharedVariables.sendUIPackets && !isVitalPacket) {
            // Log to console for debugging, but commenting out to prevent spam
            // System.out.println("Blocking packet: " + packet.getClass().getSimpleName());
            ci.cancel();
            return;
        }

        // Handle "Delay/Queueing" mode - Queue ALL packets (except vital) if delayUIPackets is true
        if (SharedVariables.delayUIPackets && !isVitalPacket) {
            SharedVariables.delayedUIPackets.add(packet);
            // System.out.println("Queuing packet: " + packet.getClass().getSimpleName());
            ci.cancel();
            return;
        }

        // Special handling for sign editing (bypass)
        // Corrected logic: Use SharedVariables to track state
        if (!SharedVariables.shouldEditSign && packet instanceof ServerboundSignUpdatePacket) {
            SharedVariables.shouldEditSign = true;
            // System.out.println("Blocking sign update packet explicitly");
            ci.cancel();
        }
    }
}
