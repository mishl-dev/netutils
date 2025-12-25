package org.netutils.mixin.accessor;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for ClientConnection internals.
 */
@Mixin(Connection.class)
public interface ClientConnectionAccessor {
    @Accessor("channel")
    Channel getChannel();
}

