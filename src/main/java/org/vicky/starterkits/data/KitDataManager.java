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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.vicky.starterkits.StarterKits;

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
        }
        else {
            StarterKits.LOGGER.error("An unexpected error occurred. (packet contains unknown kits)");
        }
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
