package org.netutils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.netutils.SharedVariables;
import org.netutils.gui.WidgetUtils;
import org.netutils.gui.ScreenInputHandler;
import org.netutils.util.SlotManager;

/**
 * Mixin for inventory/container screens.
 * Adds chat input field overlay, Sync ID/Revision display, and slot management
 * features.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Unique
    private static final Minecraft netutils$mc = Minecraft.getInstance();

    @Unique
    private EditBox netutils$chatField;

    private HandledScreenMixin() {
        super(null);
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void netutils$onInit(CallbackInfo ci) {
        if (SharedVariables.enabled) {
            // Create the standard widgets
            WidgetUtils.INSTANCE.createWidgets(netutils$mc, (Screen) (Object) this);

            // Create the chat input field overlay
            this.netutils$chatField = new EditBox(this.font, 5, 245, 160, 20, Component.literal("Chat ..."));
            this.netutils$chatField.setValue("");
            this.netutils$chatField.setMaxLength(256);
            this.netutils$chatField.setHint(Component.literal("Chat & Commands..."));
            this.addRenderableWidget(this.netutils$chatField);

            // Register the chat field with the input handler so keyboard events are
            // forwarded
            ScreenInputHandler.activeContainerChatField = this.netutils$chatField;
        }
    }

    @Inject(at = @At("HEAD"), method = "removed")
    public void netutils$onRemoved(CallbackInfo ci) {
        // Clear the reference when screen is closed
        if (this.netutils$chatField != null) {
            ScreenInputHandler.activeContainerChatField = null;
        }
        // Stop slot picking mode if active
        SlotManager.INSTANCE.stopPicking();
    }

    /**
     * Inject at HEAD of keyPressed to CANCEL the event when our chat field is
     * focused.
     * This prevents the container screen from processing keys like 'E' which would
     * close the inventory.
     */
    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void netutils$onKeyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (SharedVariables.enabled && this.netutils$chatField != null && this.netutils$chatField.isFocused()) {
            // Let the EditBox handle the key first
            boolean handled = this.netutils$chatField.keyPressed(keyEvent);

            // Cancel the event regardless - we don't want the container to process keys
            // while typing
            cir.setReturnValue(handled);
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void netutils$onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (SharedVariables.enabled && netutils$mc.player != null) {
            // Render Sync ID and Revision info
            int syncId = netutils$mc.player.containerMenu.containerId;
            int revision = netutils$mc.player.containerMenu.getStateId();

            context.drawString(this.font, "Sync Id: " + syncId, 200, 5, 0xFFFFFF, false);
            context.drawString(this.font, "Revision: " + revision, 200, 20, 0xFFFFFF, false);
        }
    }

    /**
     * Inject at TAIL of renderSlot to draw slot IDs and highlights.
     */
    @Inject(at = @At("TAIL"), method = "renderSlot")
    public void netutils$onRenderSlot(GuiGraphics graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (!SharedVariables.enabled)
            return;

        // Draw slot IDs if enabled
        if (SlotManager.INSTANCE.shouldDrawSlotIDs) {
            SlotManager.INSTANCE.drawSlotID(graphics, slot);
        }

        // Draw highlight on the selected slot
        if (SlotManager.INSTANCE.shouldRenderHighlightedSlot &&
                slot.index == SlotManager.INSTANCE.highlightedSlotID) {
            SlotManager.INSTANCE.drawHighlightedOnSlot(graphics, slot);
        }
    }

    /**
     * Inject at HEAD of slotClicked to intercept clicks during slot picking mode.
     */
    @Inject(at = @At("HEAD"), method = "slotClicked", cancellable = true)
    public void netutils$onSlotClicked(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        if (!SharedVariables.enabled)
            return;

        // Handle slot picking mode
        if (SlotManager.INSTANCE.isPicking()) {
            if (slot != null) {
                SlotManager.INSTANCE.onSlotPicked(slot.index);
            } else {
                SlotManager.INSTANCE.stopPicking();
            }
            ci.cancel(); // Prevent the actual slot click
        }
    }
}
