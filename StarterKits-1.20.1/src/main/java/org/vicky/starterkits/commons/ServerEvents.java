package org.vicky.starterkits.commons;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vicky.starterkits.config.StarterKitsConfig;
import org.vicky.starterkits.logic.ClaimedKitsProvider;
import org.vicky.starterkits.logic.GiveSelectorMode;
import org.vicky.starterkits.network.PacketHandler;
import org.vicky.starterkits.network.packets.KitListPacket;
import org.vicky.starterkits.network.packets.SyncClaimedKitsPacket;
import org.vicky.starterkits.network.packets.SyncConfigPacket;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.vicky.starterkits.StarterKits.KIT_DATA;
import static org.vicky.starterkits.StarterKits.MOD_ID;
import static org.vicky.starterkits.init.DefaultInits.makeKitSelectorItem;
import static org.vicky.starterkits.init.ModItems.KIT_SELECTOR;


@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    public static final ConcurrentMap<String, CompoundTag> STASH = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "claimed_kits");

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        player.getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(store -> {
            CompoundTag nbt = store.saveNBT();
            STASH.put(player.getUUID().toString(), nbt);
            LOGGER.debug("Saved kits in STASH for {} - {}", player.getName().getString(), nbt);
        });
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player player)) return;
        if (player.getLevel().isClientSide()) return;

        event.addCapability(ID, new ClaimedKitsProvider());
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        if (!(StarterKitsConfig.COMMON.giveSelectorMode.get().equals(GiveSelectorMode.ON_DEATH) ||
                StarterKitsConfig.COMMON.giveSelectorMode.get().equals(GiveSelectorMode.ALWAYS) ||
                StarterKitsConfig.COMMON.giveSelectorMode.get().equals(GiveSelectorMode.ON_DEATH_NOT_CLAIMED))
        ) return;
        event.getDrops().removeIf(itemEntity -> KIT_SELECTOR.get() == itemEntity.getItem().getItem());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        LOGGER.debug("STASH keys before remove: {} - {}", STASH.keySet(), event.getOriginal().getUUID());
        CompoundTag stash = STASH.remove(event.getOriginal().getUUID().toString());
        if (stash != null) {
            LOGGER.debug("Restoring kits from STASH for {}", event.getOriginal().getName().getString());

            event.getEntity().getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(newStore -> {
                newStore.loadNBT(stash);
                LOGGER.debug("Restored kits from old player on clone: {}", stash);
            });
        }

        if(event.isWasDeath()) {
            var item = makeKitSelectorItem(
                    ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(MOD_ID, "kit_selector")),
                    StarterKitsConfig.COMMON.kitSelectorItemName.get(),
                    StarterKitsConfig.COMMON.kitSelectorItemLore.get(),
                    StarterKitsConfig.COMMON.kitMaxUsages.get()
            );

            // ON_DEATH_NOT_CLAIMED: only give if they haven't rolled+claimed
            if (StarterKitsConfig.COMMON.giveSelectorMode.get() == GiveSelectorMode.ON_DEATH_NOT_CLAIMED) {
                event.getOriginal().getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(storage -> {
                    if (event.getEntity() instanceof ServerPlayer player && !storage.hasRolledOnceAndClaimed() && playerNotHasSelector(player)) {
                        player.getInventory().add(item);
                    }
                });
            }
            // ON_DEATH or ALWAYS (but for ONCE we do NOT enter here)
            else if (StarterKitsConfig.COMMON.giveSelectorMode.get() == GiveSelectorMode.ON_DEATH
                    || StarterKitsConfig.COMMON.giveSelectorMode.get() == GiveSelectorMode.ALWAYS) {
                event.getOriginal().getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(storage -> {
                    if (event.getEntity() instanceof ServerPlayer player && playerNotHasSelector(player)) {
                        player.getInventory().add(item);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onClientJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        KIT_DATA.reloadKitsFromConfigFolder();
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            LOGGER.info("Sending kits list to client manager...");
            var kits = KIT_DATA.getAllKits();
            PacketHandler.INSTANCE.sendTo(
                    new KitListPacket(new ArrayList<>(kits)),
                    serverPlayer.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
            PacketHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new SyncConfigPacket(
                            StarterKitsConfig.COMMON.kitSelectorItemName.get(),
                            StarterKitsConfig.COMMON.kitSelectorItemLore.get(),
                            StarterKitsConfig.COMMON.kitMaxUsages.get(),
                            StarterKitsConfig.COMMON.kitIsSelectable.get(),
                            StarterKitsConfig.COMMON.allowRollableKits.get()
                    )
            );

            var item = makeKitSelectorItem(
                    ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(MOD_ID, "kit_selector")),
                    StarterKitsConfig.COMMON.kitSelectorItemName.get(),
                    StarterKitsConfig.COMMON.kitSelectorItemLore.get(),
                    StarterKitsConfig.COMMON.kitMaxUsages.get()
            );

            if (StarterKitsConfig.COMMON.giveSelectorMode.get() == GiveSelectorMode.ALWAYS) {
                serverPlayer.getInventory().add(item);
            } else if (StarterKitsConfig.COMMON.giveSelectorMode.get() == GiveSelectorMode.ONCE || StarterKitsConfig.COMMON.giveSelectorMode.get() == GiveSelectorMode.ON_DEATH) {
                serverPlayer.getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(storage -> {
                    if (!storage.hasGottenFirstJoinKit()) {
                        storage.setHasGottenFirstJoinKit(true);
                        serverPlayer.getInventory().add(item);
                    }
                });
            }

            serverPlayer.getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(cap -> {
                var claimed = cap.getClaimedKits();
                PacketHandler.INSTANCE.sendTo(
                        new SyncClaimedKitsPacket(new ArrayList<>(claimed)),
                        serverPlayer.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                );
            });
        }
    }

    private static boolean playerNotHasSelector(ServerPlayer player) {
        // Check both main inventory and offhand
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == KIT_SELECTOR.get()) return false;
        }
        for (ItemStack stack : player.getInventory().armor) { // just in case
            if (stack.getItem() == KIT_SELECTOR.get()) return false;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() == KIT_SELECTOR.get()) return false;
        }
        return true;
    }
}
