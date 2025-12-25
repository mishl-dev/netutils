package org.netutils.forge

import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.netutils.SharedVariables

class NetUtilsConfigScreen(private val parent: Screen) : Screen(Component.literal("NetUtils Configuration")) {

    override fun init() {
        val layout = LinearLayout.vertical().spacing(8)
        layout.defaultCellSetting().alignHorizontallyCenter()

        layout.addChild(StringWidget(this.title, this.font))

        // Enabled Toggle
        layout.addChild(Button.builder(
            Component.literal("NetUtils Enabled: " + if (SharedVariables.enabled) "ON" else "OFF")
        ) { button: Button ->
            SharedVariables.enabled = !SharedVariables.enabled
            button.message = Component.literal("NetUtils Enabled: " + if (SharedVariables.enabled) "ON" else "OFF")
        }.width(200).build())

        // Resource Pack Bypass Toggle
        layout.addChild(Button.builder(
            Component.literal("RP Bypass: " + if (SharedVariables.bypassResourcePack) "ON" else "OFF")
        ) { button: Button ->
            SharedVariables.bypassResourcePack = !SharedVariables.bypassResourcePack
            button.message = Component.literal("RP Bypass: " + if (SharedVariables.bypassResourcePack) "ON" else "OFF")
        }.width(200).build())

        // Force Deny Toggle
        layout.addChild(Button.builder(
            Component.literal("RP Force Deny: " + if (SharedVariables.resourcePackForceDeny) "ON" else "OFF")
        ) { button: Button ->
            SharedVariables.resourcePackForceDeny = !SharedVariables.resourcePackForceDeny
            button.message = Component.literal("RP Force Deny: " + if (SharedVariables.resourcePackForceDeny) "ON" else "OFF")
        }.width(200).build())

        // Back Button
        layout.addChild(Button.builder(Component.literal("Done")) { button: Button ->
            this.minecraft?.setScreen(this.parent)
        }.width(200).build())

        layout.arrangeElements()
        // Manual centering
        layout.setPosition(this.width / 2 - layout.width / 2, this.height / 2 - layout.height / 2)
        layout.visitWidgets { this.addRenderableWidget(it) }
    }

    override fun onClose() {
        this.minecraft?.setScreen(this.parent)
    }
}
