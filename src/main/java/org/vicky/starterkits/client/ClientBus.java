package org.vicky.starterkits.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vicky.starterkits.StarterKits;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = StarterKits.MOD_ID)
public class ClientBus {
    @SubscribeEvent
    public static void onClientLeaves(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        StarterKits.LOGGER.info("Client disconnected, clearing kits...");
        ClientKitManager.INSTANCE.clearKits();
        ClientClaimedKitsManager.INSTANCE.clear();
    }
}