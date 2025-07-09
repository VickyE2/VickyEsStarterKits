package org.vicky.starterkits.init;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.network.packets.KitListPacket;
import org.vicky.starterkits.network.PacketHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.vicky.starterkits.StarterKits.KIT_DATA;

public class DefaultInits {
    public static Thread kitWatcherThread;
    public static WatchService kitWatcher;

    public DefaultInits() {
    }

    public void ensureDefaultKits() {
        Path configFolder = FMLPaths.CONFIGDIR.get().resolve("starterkits/kits");
        try {
            if (Files.notExists(configFolder)) {
                Files.createDirectories(configFolder);
            }
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(configFolder)) {
                if (!dirStream.iterator().hasNext()) {
                    StarterKits.LOGGER.info("No kits found in config; copying defaults...");
                    copyDefaultKitsFromJar(configFolder);
                }
            }
        } catch (IOException e) {
            StarterKits.LOGGER.error("Failed to prepare kits config folder", e);
        }
    }

    public void copyDefaultKitsFromJar(Path targetFolder) {
        String defaultsPath = "data/starterkits/kits";

        try {
            // ClassLoader from mod
            ClassLoader classLoader = StarterKits.class.getClassLoader();

            // Hardcoded default file names (best if you know them)
            List<String> defaultFiles = List.of("default.json"); // replace with yours

            for (String fileName : defaultFiles) {
                try (InputStream in = classLoader.getResourceAsStream(defaultsPath + "/" + fileName)) {
                    if (in != null) {
                        Path targetFile = targetFolder.resolve(fileName);
                        Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        StarterKits.LOGGER.info("Copied default kit: {}", fileName);
                    } else {
                        StarterKits.LOGGER.warn("Default kit file not found in jar: {}", fileName);
                    }
                }
            }

        } catch (IOException e) {
            StarterKits.LOGGER.error("Failed to copy default kits from jar", e);
        }
    }

    public static void startFileWatcher() throws IOException {
        if (kitWatcherThread != null) {
            kitWatcherThread.stop();
            kitWatcher.close();
        }

        Path kitsFolder = Paths.get(FMLPaths.CONFIGDIR.get().toString(), "starterkits", "kits");
        kitWatcher = FileSystems.getDefault().newWatchService();
        kitsFolder.register(kitWatcher, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);

        kitWatcherThread = new Thread(() -> {
            try {
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                AtomicReference<ScheduledFuture<?>> pendingReload = new AtomicReference<>();

                while (true) {
                    WatchKey key = kitWatcher.poll(500, TimeUnit.MILLISECONDS);
                    if (key == null) continue;

                    for (WatchEvent<?> event : key.pollEvents()) {
                        // StarterKits.LOGGER.info("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                        if (pendingReload.get() != null) {
                            pendingReload.get().cancel(false);
                        }
                        pendingReload.set(
                                scheduler.schedule(() -> {
                                    KIT_DATA.reloadKitsFromConfigFolder();
                                    StarterKits.LOGGER.info("Kit config changed, reloaded!");

                                    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                                    if (server != null) {
                                        server.execute(() -> {
                                            var kits = StarterKits.KIT_DATA.getAllKits();
                                            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                                                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new KitListPacket(kits.stream().toList()));
                                            }
                                            StarterKits.LOGGER.info("'Packeted' players'");
                                        });
                                    }
                                }, 300, TimeUnit.MILLISECONDS)
                        );
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        kitWatcherThread.start();
    }

    public static ItemStack makeKitSelectorItem(Item item, String name, List<String> loreStrings, int maxUsages) {
        ItemStack stack = new ItemStack(item);
        stack.setHoverName(ComponentUtil.colorize(name));
        List<Component> loreComponents = loreStrings.stream()
                .map(ComponentUtil::colorize)
                .toList();
        ListTag loreTag = new ListTag();
        for (Component c : loreComponents) {
            loreTag.add(StringTag.valueOf(Component.Serializer.toJson(c)));
        }
        loreTag.add(StringTag.valueOf("{\"text\":\"§6Max Usages: " + maxUsages + "\"}"));
        loreTag.add(StringTag.valueOf("{\"text\":\"§6Usages Left: " + maxUsages + "\"}"));
        stack.getOrCreateTagElement("display").put("Lore", loreTag);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("MaxUses", maxUsages);
        tag.putInt("UsesLeft", maxUsages);
        return stack;
    }
}