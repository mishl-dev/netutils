package org.netutils.forge;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.netutils.NetUtilsCommon;

/**
 * NeoForge entry point for NetUtils.
 */
@Mod(NetUtilsCommon.MOD_ID)
public class NetUtilsForge {
    public NetUtilsForge() {
        NetUtilsCommon.INSTANCE.init();
        
        if (FMLEnvironment.getDist().isClient()) {
            ClientProxy.registerConfigScreen();
        }
    }

    private static class ClientProxy {
        static void registerConfigScreen() {
            ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (minecraft, parent) -> new NetUtilsConfigScreen(parent)
            );
        }
    }
}
