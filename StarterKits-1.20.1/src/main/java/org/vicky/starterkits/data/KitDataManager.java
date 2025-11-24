package org.vicky.starterkits.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.commons.CommonsVariables;
import org.vicky.starterkits.commons.slotable_helpers.CuriosHelper;
import org.vicky.starterkits.commons.slotable_helpers.SlotHelper;
import org.vicky.starterkits.logic.ClaimedKitsProvider;
import org.vicky.starterkits.network.PacketHandler;
import org.vicky.starterkits.network.packets.SyncClaimedKitsPacket;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class KitDataManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Kit.class, new KitDeserializer())
            .registerTypeAdapter(Kit.KitItem.class, new KitDeserializer.KitItemDeserializer())
            .registerTypeAdapter(Kit.KitSlotable.class, new KitDeserializer.KitSlotableDeserializer())
            .create();

    private Map<String, Kit> kits = new HashMap<>();

    public KitDataManager() {
        super(GSON, "kits"); // looks in data/<modid>/kits/*.json
    }

    public void giveKitToPlayer(ServerPlayer player, String kitName) {
        Kit kit = kits.get(kitName);
        if (kit != null) {
            for (Kit.KitItem item : kit.items) {
                ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item.item)), item.count);
                if (item.nbt != null && !item.nbt.isEmpty()) {
                    try {
                        stack.setTag(net.minecraft.nbt.TagParser.parseTag(item.nbt));
                    } catch (Exception ignored) {}
                }
                player.getInventory().add(stack);
            }
            for (Kit.KitSlotable slotable : kit.slotables) {
                String[] parts = slotable.item.split(":");
                ResourceLocation itemId = new ResourceLocation(parts[0], parts[1]);
                int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
                ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(itemId), count);
                if (slotable.nbt != null && !slotable.nbt.isEmpty()) {
                    try {
                        stack.setTag(net.minecraft.nbt.TagParser.parseTag(slotable.nbt));
                    } catch (Exception ignored) {}
                }
                if (CuriosHelper.isCuriousSlot(slotable.slot)) {
                    if (CommonsVariables.curiosLoaded)
                        CuriosHelper.giveToSlot(player, slotable.slot, stack);
                    else {
                        player.sendMessage(ComponentUtil.createTranslated("§cCurios mod not installed: cannot equip to slot '" + slotable.slot + "'"), player.getUUID());
                        StarterKits.LOGGER.warn("Curios mod not installed: cannot equip to slot '{}'", slotable.slot);
                        player.addItem(stack);
                    }
                } else {
                    SlotHelper.giveToSlot(player, slotable.slot, stack);
                }
            }
        }
        else {
            StarterKits.LOGGER.error("An unexpected error occurred. (packet contains unknown kits)");
        }
    }

    public Kit getRandomKit(Player player) {
        List<Kit> kits = getAllAvailableKitsFor(player); // filter by permission, usage etc
        if (kits.isEmpty()) return null;
        int totalWeight = kits.stream().mapToInt(k -> k.weight).sum();
        int roll = player.getRandom().nextInt(totalWeight);
        int cumulative = 0;
        for (Kit kit : kits) {
            cumulative += kit.weight;
            if (roll < cumulative) {
                return kit;
            }
        }
        return kits.get(0);
    }

    private List<Kit> getAllAvailableKitsFor(Player player) {
        List<Kit> available = new ArrayList<>();
        for (var kit : kits.values()) {
            player.getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(cap -> {
                var claimed = cap.getClaimedKits();
                if (kit.canClaimKit(player) && !claimed.contains(kit.name)) {
                    available.add(kit);
                }
            });
        }
        return available;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<String, Kit> newKits = new HashMap<>();

        jsonMap.forEach((id, json) -> {
            try {
                Kit kit = GSON.fromJson(json, Kit.class);
                if (kit != null && kit.name != null) {
                    newKits.put(kit.name, kit);
                }
            } catch (Exception e) {
                StarterKits.LOGGER.error("Failed to parse kit {}", id, e);
            }
        });

        this.kits = newKits;
        StarterKits.LOGGER.info("Loaded {} kits!", kits.size());
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public Collection<Kit> getAllKits() {
        return kits.values();
    }

    public void reloadKitsFromConfigFolder() {
        Map<String, Kit> newKits = new HashMap<>();

        Path kitsFolder = Paths.get(FMLPaths.CONFIGDIR.get().toString(), "starterkits", "kits");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(kitsFolder, "*.json")) {
            for (Path entry : stream) {
                try (Reader reader = Files.newBufferedReader(entry)) {
                    Kit kit = GSON.fromJson(reader, Kit.class);
                    if (kit != null && kit.name != null) {
                        newKits.put(kit.name, kit);
                    }
                } catch (Exception e) {
                    StarterKits.LOGGER.error("Failed to load kit {}", entry.getFileName(), e);
                }
            }
        } catch (IOException e) {
            StarterKits.LOGGER.error("Failed to read kits folder", e);
        }

        kits = newKits;
        StarterKits.LOGGER.info("Reloaded {} kits from config folder!", kits.size());
    }

}
