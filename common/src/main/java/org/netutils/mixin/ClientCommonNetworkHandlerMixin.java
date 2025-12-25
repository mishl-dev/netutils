package org.netutils.mixin;

import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.netutils.SharedVariables;
import org.netutils.NetUtilsCommon;
/**
 * Mixin for resource pack handling.
 * Allows bypassing or denying server resource packs.
 */
@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonNetworkHandlerMixin {
    
    @Inject(method = "handleResourcePackPush", at = @At("HEAD"), cancellable = true)
    private void onResourcePackSend(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (SharedVariables.bypassResourcePack) {
            // Pretend we accepted and loaded the pack
            NetUtilsCommon.LOGGER.info("Resource pack bypassed: {}", packet.url());
            ci.cancel();
        } else if (SharedVariables.resourcePackForceDeny) {
            // Decline the resource pack
            NetUtilsCommon.LOGGER.info("Resource pack denied: {}", packet.url());
            ci.cancel();
        }
    }
}

