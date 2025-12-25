package org.netutils.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import org.netutils.SharedVariables;

public class NetUtilsModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new NetUtilsConfigScreen(parent);
    }
    
    private static class NetUtilsConfigScreen extends Screen {
        private final Screen parent;

        protected NetUtilsConfigScreen(Screen parent) {
            super(Component.literal("NetUtils Configuration"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            LinearLayout layout = LinearLayout.vertical().spacing(8);
            layout.defaultCellSetting().alignHorizontallyCenter();
            
            layout.addChild(new StringWidget(this.title, this.font));
            
            // Enabled Toggle
            layout.addChild(Button.builder(
                Component.literal("NetUtils Enabled: " + (SharedVariables.enabled ? "ON" : "OFF")),
                button -> {
                    SharedVariables.enabled = !SharedVariables.enabled;
                    button.setMessage(Component.literal("NetUtils Enabled: " + (SharedVariables.enabled ? "ON" : "OFF")));
                }
            ).width(200).build());

            // Allow KeepAlive Toggle
            layout.addChild(Button.builder(
                Component.literal("Allow KeepAlive: " + (SharedVariables.allowKeepAlive ? "ON" : "OFF")),
                button -> {
                    SharedVariables.allowKeepAlive = !SharedVariables.allowKeepAlive;
                    button.setMessage(Component.literal("Allow KeepAlive: " + (SharedVariables.allowKeepAlive ? "ON" : "OFF")));
                }
            ).width(200).build());

            // Resource Pack Bypass Toggle
            layout.addChild(Button.builder(
                Component.literal("RP Bypass: " + (SharedVariables.bypassResourcePack ? "ON" : "OFF")),
                button -> {
                    SharedVariables.bypassResourcePack = !SharedVariables.bypassResourcePack;
                    button.setMessage(Component.literal("RP Bypass: " + (SharedVariables.bypassResourcePack ? "ON" : "OFF")));
                }
            ).width(200).build());
            
             // Force Deny Toggle
            layout.addChild(Button.builder(
                Component.literal("RP Force Deny: " + (SharedVariables.resourcePackForceDeny ? "ON" : "OFF")),
                button -> {
                    SharedVariables.resourcePackForceDeny = !SharedVariables.resourcePackForceDeny;
                    button.setMessage(Component.literal("RP Force Deny: " + (SharedVariables.resourcePackForceDeny ? "ON" : "OFF")));
                }
            ).width(200).build());
            
             // Back Button
            layout.addChild(Button.builder(Component.literal("Done"), button -> {
                this.minecraft.setScreen(this.parent);
            }).width(200).build());

            layout.arrangeElements();
            // Manual centering to avoid API mismatch
            layout.setPosition(this.width / 2 - layout.getWidth() / 2, this.height / 2 - layout.getHeight() / 2);
            layout.visitWidgets(this::addRenderableWidget);
        }
        
        @Override
        public void onClose() {
            this.minecraft.setScreen(this.parent);
        }
    }
}
