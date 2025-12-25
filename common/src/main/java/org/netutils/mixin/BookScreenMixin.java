package org.netutils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.netutils.SharedVariables;
import org.netutils.gui.WidgetUtils;

@Mixin(BookViewScreen.class)
public abstract class BookScreenMixin extends Screen {

    protected BookScreenMixin(Component component) {
        super(component);
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        if (SharedVariables.enabled) {
            WidgetUtils.INSTANCE.createWidgets(Minecraft.getInstance(), this);
        }
    }
}
