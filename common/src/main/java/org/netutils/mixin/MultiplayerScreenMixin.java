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

            // Position in bottom-left corner
            int leftX = 5;
            int buttonWidth = 160;
            int bottomY = this.height - 30; // First button from bottom

            // Force Deny Toggle - bottom button
            accessor.invokeAddRenderableWidget(Button.builder(
                    Component.literal("Force Deny: " + (SharedVariables.resourcePackForceDeny ? "ON" : "OFF")),
                    (button) -> {
                        SharedVariables.resourcePackForceDeny = !SharedVariables.resourcePackForceDeny;
                        button.setMessage(Component
                                .literal("Force Deny: " + (SharedVariables.resourcePackForceDeny ? "ON" : "OFF")));
                    }).bounds(leftX, bottomY, buttonWidth, 20).build());

            // Bypass Resource Pack Toggle - above Force Deny
            accessor.invokeAddRenderableWidget(Button.builder(
                    Component.literal("Bypass Resource Pack: " + (SharedVariables.bypassResourcePack ? "ON" : "OFF")),
                    (button) -> {
                        SharedVariables.bypassResourcePack = !SharedVariables.bypassResourcePack;
                        button.setMessage(Component.literal(
                                "Bypass Resource Pack: " + (SharedVariables.bypassResourcePack ? "ON" : "OFF")));
                    }).bounds(leftX, bottomY - 22, buttonWidth, 20).build());
        }
    }
}
