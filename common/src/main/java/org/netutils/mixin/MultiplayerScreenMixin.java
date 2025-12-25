package org.netutils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.netutils.SharedVariables;
import org.netutils.mixin.accessor.ScreenAccessor;

@Mixin(JoinMultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {

    protected MultiplayerScreenMixin(Component component) {
        super(component);
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        if (SharedVariables.enabled) {
            ScreenAccessor accessor = (ScreenAccessor) this;

            // Center X position
            int centerX = this.width / 2;
            int buttonWidth = 160;

            // Bypass Resource Pack Toggle - positioned above the server list area
            accessor.invokeAddRenderableWidget(Button.builder(
                    Component.literal("Bypass Resource Pack: " + (SharedVariables.bypassResourcePack ? "ON" : "OFF")),
                    (button) -> {
                        SharedVariables.bypassResourcePack = !SharedVariables.bypassResourcePack;
                        button.setMessage(Component.literal(
                                "Bypass Resource Pack: " + (SharedVariables.bypassResourcePack ? "ON" : "OFF")));
                    }).bounds(centerX - buttonWidth / 2, 50, buttonWidth, 20).build());

            // Force Deny Toggle
            accessor.invokeAddRenderableWidget(Button.builder(
                    Component.literal("Force Deny: " + (SharedVariables.resourcePackForceDeny ? "ON" : "OFF")),
                    (button) -> {
                        SharedVariables.resourcePackForceDeny = !SharedVariables.resourcePackForceDeny;
                        button.setMessage(Component
                                .literal("Force Deny: " + (SharedVariables.resourcePackForceDeny ? "ON" : "OFF")));
                    }).bounds(centerX - buttonWidth / 2, 75, buttonWidth, 20).build());
        }
    }
}
