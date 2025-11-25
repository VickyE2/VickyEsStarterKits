package org.vicky.starterkits;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vicky.starterkits.config.StarterKitsConfig;
import org.vicky.starterkits.data.KitDataManager;
import org.vicky.starterkits.init.DefaultInits;
import org.vicky.starterkits.init.ModItems;
import org.vicky.starterkits.network.PacketHandler;

import java.io.IOException;

import static org.vicky.starterkits.init.ModItems.*;

@Mod(StarterKits.MOD_ID)
public class StarterKits {

    public static final String MOD_ID = "starterkits";
    public static Logger LOGGER;

    public static KitDataManager KIT_DATA = new KitDataManager();
    private final DefaultInits defaultInits = new DefaultInits();

    public StarterKits() {
        LOGGER = LogManager.getLogger(MOD_ID);
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        registerSetup(modBus);
        ModItems.ITEMS.register(modBus);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                StarterKitsConfig.COMMON_SPEC
        );

        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(KIT_SELECTOR);
            event.accept(KIT_CREATOR);
        }
    }

    private void registerSetup(IEventBus modBus) {
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onLoadComplete);
    }

    private void onLoadComplete(FMLLoadCompleteEvent event) {
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent e) -> e.addListener(KIT_DATA));
        KIT_DATA.reloadKitsFromConfigFolder();
        try {
            DefaultInits.startFileWatcher();
        }
        catch (IOException e) {
            LOGGER.error("Failed to start file watcher for kits.... did something go wrong?");
            e.printStackTrace();
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(defaultInits::ensureDefaultKits);
        event.enqueueWork(PacketHandler::register);
    }
}
