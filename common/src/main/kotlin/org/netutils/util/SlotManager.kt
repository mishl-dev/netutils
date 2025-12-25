package org.netutils.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.Slot

/**
 * Manages slot visualization features like:
 * - Drawing slot IDs on inventory screens
 * - Highlighting specific slots
 * - Slot picking mode for selecting slots by clicking
 */
object SlotManager {
    
    // Drawing settings
    @JvmField var shouldDrawSlotIDs: Boolean = false
    var slotIDTextColor: Int = 0xFF333333.toInt()
    
    // Highlighting settings
    @JvmField var highlightedSlotID: Int = -1
    @JvmField var shouldRenderHighlightedSlot: Boolean = false
    var highlightColor: Int = 0x80C7007F.toInt() // Semi-transparent pink
    
    // Slot picking mode
    private var _isPicking: Boolean = false
    var shouldStopPicking: Boolean = false
    
    /**
     * Check if currently in picking mode. (Java-friendly)
     */
    fun isPicking(): Boolean = _isPicking
    
    private val mc = Minecraft.getInstance()
    
    /**
     * Draw the slot ID number on a slot.
     * 
     * @param graphics The GuiGraphics context
     * @param slot The slot to draw on
     */
    fun drawSlotID(graphics: GuiGraphics, slot: Slot) {
        if (mc.screen == null) return
        
        val text = Component.literal(slot.index.toString())
            .withStyle { it.withColor(slotIDTextColor) }
        
        val font = mc.font
        val textWidth = font.width(text)
        val textX = slot.x + 8 - textWidth / 2
        val textY = slot.y + 8 - 4
        
        graphics.drawString(font, text, textX, textY, slotIDTextColor, false)
    }
    
    /**
     * Draw a highlight overlay on a slot.
     * 
     * @param graphics The GuiGraphics context
     * @param slot The slot to highlight
     */
    fun drawHighlightedOnSlot(graphics: GuiGraphics, slot: Slot) {
        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, highlightColor)
        drawSlotID(graphics, slot)
    }
    
    /**
     * Start slot picking mode.
     * In this mode, clicking a slot will select it instead of interacting with it.
     */
    fun startPicking() {
        _isPicking = true
        shouldStopPicking = false
        highlightedSlotID = -1
        mc.player?.displayClientMessage(
            Component.literal("§e[NetUtils] Click a slot to select it..."),
            true
        )
    }
    
    /**
     * Stop slot picking mode.
     */
    fun stopPicking() {
        _isPicking = false
        shouldStopPicking = false
    }
    
    /**
     * Handle when a slot is selected during picking mode.
     * 
     * @param slotId The selected slot ID
     */
    fun onSlotPicked(slotId: Int) {
        highlightedSlotID = slotId
        shouldRenderHighlightedSlot = true
        _isPicking = false // Stop picking after selection
        shouldStopPicking = false
        mc.player?.displayClientMessage(
            Component.literal("§a[NetUtils] Selected slot: $slotId"),
            true
        )
    }
    
    /**
     * Toggle slot ID display.
     */
    fun toggleSlotIDs() {
        shouldDrawSlotIDs = !shouldDrawSlotIDs
        mc.player?.displayClientMessage(
            Component.literal("§e[NetUtils] Slot IDs: ${if (shouldDrawSlotIDs) "ON" else "OFF"}"),
            true
        )
    }
    
    /**
     * Toggle slot highlighting.
     */
    fun toggleHighlight() {
        shouldRenderHighlightedSlot = !shouldRenderHighlightedSlot
    }
    
    /**
     * Clear the highlighted slot.
     */
    fun clearHighlight() {
        highlightedSlotID = -1
        shouldRenderHighlightedSlot = false
    }
}
