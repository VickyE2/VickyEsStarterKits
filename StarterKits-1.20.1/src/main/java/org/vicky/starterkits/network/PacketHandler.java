package org.vicky.starterkits.network;


import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.network.packets.*;

import java.util.Optional;

import static org.vicky.starterkits.StarterKits.LOGGER;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1.3";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(StarterKits.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        log("Registering KitListPacket at ID " + packetId);
        INSTANCE.registerMessage(packetId++, KitListPacket.class,
                KitListPacket::encode, KitListPacket::decode, KitListPacket::handle);
        log("Registering ChooseKitPacket at ID " + packetId);
        INSTANCE.registerMessage(packetId++, ChooseKitPacket.class,
                ChooseKitPacket::encode, ChooseKitPacket::decode, ChooseKitPacket::handle);
        log("Registering RequestRandomKitPacket at ID " + packetId);
        INSTANCE.registerMessage(packetId++, RequestRandomKitPacket.class,
                RequestRandomKitPacket::encode, RequestRandomKitPacket::decode, RequestRandomKitPacket::handle);
        log("Registering CreateKitPacket at ID " + packetId);
        INSTANCE.registerMessage(
                packetId++, CreateKitPacket.class,
                CreateKitPacket::encode, CreateKitPacket::decode, CreateKitPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        log("Registering SyncClaimedKitsPacket at ID " + packetId);
        INSTANCE.messageBuilder(SyncClaimedKitsPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncClaimedKitsPacket::encode)
                .decoder(SyncClaimedKitsPacket::decode)
                .consumerMainThread(SyncClaimedKitsPacket::handle)
                .add();
        log("Registering SyncConfigPacket at ID " + packetId);
        INSTANCE.registerMessage(packetId++, SyncConfigPacket.class,
                        SyncConfigPacket::encode, SyncConfigPacket::decode, SyncConfigPacket::handle,
                        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        log("Registering OpenKitSelectorScreenPacket at ID " + packetId);
        INSTANCE.registerMessage(packetId++, OpenKitSelectorScreenPacket.class,
                 OpenKitSelectorScreenPacket::encode, OpenKitSelectorScreenPacket::decode, OpenKitSelectorScreenPacket::handle,
                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        log("Registering RandomKitSelectionResultPacket at ID " + packetId);
        INSTANCE.registerMessage(packetId++, RandomKitSelectionResultPacket.class,
                 RandomKitSelectionResultPacket::encode, RandomKitSelectionResultPacket::decode, RandomKitSelectionResultPacket::handle,
                 Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        log("Registering AutoRollForUser at ID " + packetId);
        INSTANCE.registerMessage(packetId++, AutoRollForUser.class,
                AutoRollForUser::encode, AutoRollForUser::decode, AutoRollForUser::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private static void log(String msg) {
        LOGGER.debug("[StarterKits::PacketHandler] " + msg);
    }
}