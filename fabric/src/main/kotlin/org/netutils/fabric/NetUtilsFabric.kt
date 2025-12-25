package org.netutils.fabric

import net.fabricmc.api.ClientModInitializer
import org.netutils.NetUtilsCommon

/**
 * Fabric-specific entry point for NetUtils.
 */
object NetUtilsFabric : ClientModInitializer {
    override fun onInitializeClient() {
        NetUtilsCommon.init()
    }
}

