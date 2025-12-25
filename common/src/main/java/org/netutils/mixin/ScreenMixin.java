package org.netutils.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.netutils.SharedVariables;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.netutils.gui.WidgetUtils;
import net.minecraft.client.Minecraft;

/**
 * Base screen mixin for common functionality.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    
    @Shadow
    public int width;
    
    @Shadow
    public int height;
    
    @Shadow
    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (SharedVariables.enabled) {
            Minecraft mc = Minecraft.getInstance();
            // Check if we are in a ChatScreen (which SleepingChatScreen extends) and the player is sleeping
            if (((Object)this) instanceof ChatScreen && mc.player != null && mc.player.isSleeping()) {
                WidgetUtils.INSTANCE.createWidgets(mc, (Screen)(Object)this);
            }
        }
    }

}
