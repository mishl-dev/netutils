package org.netutils.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.netutils.command.ClientCommands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

/**
 * Mixin to add autocomplete suggestions for NetUtils client commands.
 */
@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    @Shadow
    @Final
    EditBox input;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    public abstract void showSuggestions(boolean narrateFirstSuggestion);

    /**
     * Inject into updateCommandInfo to add our custom command suggestions.
     */
    @Inject(at = @At("HEAD"), method = "updateCommandInfo", cancellable = true)
    private void netutils$onUpdateCommandInfo(CallbackInfo ci) {
        String text = this.input.getValue();

        // Check if the input starts with our command prefix
        if (text.startsWith(ClientCommands.PREFIX)) {
            // Get suggestions from our command system
            java.util.List<String> suggestions = ClientCommands.INSTANCE.getSuggestions(text);

            if (!suggestions.isEmpty()) {
                // Create suggestion from our commands
                int start = 0;
                com.mojang.brigadier.suggestion.SuggestionsBuilder builder = new com.mojang.brigadier.suggestion.SuggestionsBuilder(
                        text, start);

                for (String suggestion : suggestions) {
                    builder.suggest(suggestion);
                }

                this.pendingSuggestions = CompletableFuture.completedFuture(builder.build());
                this.showSuggestions(false);
                ci.cancel();
            }
        }
    }
}
