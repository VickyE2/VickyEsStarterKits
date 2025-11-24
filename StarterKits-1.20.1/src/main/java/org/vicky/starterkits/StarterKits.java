package org.vicky.starterkits;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.config.StarterKitsConfig;
import org.vicky.starterkits.data.KitDataManager;
import org.vicky.starterkits.init.DefaultInits;
import org.vicky.starterkits.init.ModItems;
import org.vicky.starterkits.logic.ClaimedKitsProvider;
import org.vicky.starterkits.logic.GiveSelectorMode;
import org.vicky.starterkits.network.PacketHandler;
import org.vicky.starterkits.network.packets.KitListPacket;
import org.vicky.starterkits.network.packets.SyncClaimedKitsPacket;
import org.vicky.starterkits.network.packets.SyncConfigPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.vicky.starterkits.init.DefaultInits.makeKitSelectorItem;
import static org.vicky.starterkits.init.ModItems.ITEMS;
import static org.vicky.starterkits.init.ModItems.KIT_SELECTOR;

@Mod(StarterKits.MOD_ID)
public class StarterKits {

    public static final String MOD_ID = "starterkits";
    public static final String MOD_NAME = "Starter Kits";
    public static final String VERSION = "1.18.2-0.0.1-ALPHA";
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
