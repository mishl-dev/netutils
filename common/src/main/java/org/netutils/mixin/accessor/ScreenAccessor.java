package org.netutils.mixin.accessor;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.components.events.GuiEventListener;
import java.util.List;

/**
 * Accessor for Screen internals.
 */
@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("children")
    List<GuiEventListener> getChildren();

    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T invokeAddRenderableWidget(T widget);
}

