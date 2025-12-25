package org.netutils.mixin;

import net.minecraft.client.gui.screens.ChatScreen;
import org.netutils.command.ClientCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept chat messages and handle client-side commands.
 */
@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    /**
     * Intercept the handleChatInput method to handle client commands.
     * This method is called when the user presses Enter in the chat.
     * In 1.21.11, handleChatInput returns void, so we use CallbackInfo.
     */
    @Inject(at = @At("HEAD"), method = "handleChatInput", cancellable = true)
    private void netutils$onHandleChatInput(String message, boolean addToHistory, CallbackInfo ci) {
        // Check if this is a NetUtils command
        if (message.startsWith(ClientCommands.PREFIX)) {
            // Try to execute the command
            boolean handled = ClientCommands.INSTANCE.tryExecute(message);
            if (handled) {
                // Cancel the original method - command was handled
                ci.cancel();
            }
        }
    }
}
