package org.vicky.starterkits.network;


import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.network.packets.*;

import java.util.Optional;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1.3";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StarterKits.MOD_ID, "main"),
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
                .consumer(SyncClaimedKitsPacket::handle)
                .add();
        log("Registering SyncConfigPacket at ID " + packetId);
        INSTANCE.messageBuilder(SyncConfigPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncConfigPacket::encode)
                .decoder(SyncConfigPacket::decode)
                .consumer(SyncConfigPacket::handle)
                .add();
        log("Registering OpenKitSelectorScreenPacket at ID " + packetId);
        INSTANCE.messageBuilder(OpenKitSelectorScreenPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenKitSelectorScreenPacket::decode)
                .encoder(OpenKitSelectorScreenPacket::encode)
                .consumer(OpenKitSelectorScreenPacket::handle)
                .add();
        log("Registering RandomKitSelectionResultPacket at ID " + packetId);
        INSTANCE.messageBuilder(RandomKitSelectionResultPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RandomKitSelectionResultPacket::encode)
                .decoder(RandomKitSelectionResultPacket::decode)
                .consumer(RandomKitSelectionResultPacket::handle)
                .add();
    }

    private static void log(String msg) {
        System.out.println("[StarterKits::PacketHandler] " + msg);
    }
}