package org.vicky.starterkits.commons;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.logic.ClaimedKitsStorage;

@Mod.EventBusSubscriber(modid = StarterKits.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerModEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        LOGGER.debug("Registering capabilities");
        event.register(ClaimedKitsStorage.class);
    }

}
