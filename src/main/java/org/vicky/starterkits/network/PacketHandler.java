package org.vicky.starterkits.network;


import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.network.packets.*;

import java.util.Optional;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StarterKits.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++, KitListPacket.class,
                KitListPacket::encode, KitListPacket::decode, KitListPacket::handle);
        INSTANCE.registerMessage(packetId++, ChooseKitPacket.class,
                ChooseKitPacket::encode, ChooseKitPacket::decode, ChooseKitPacket::handle);
        INSTANCE.registerMessage(
                packetId++, CreateKitPacket.class,
                CreateKitPacket::encode, CreateKitPacket::decode, CreateKitPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.messageBuilder(SyncClaimedKitsPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncClaimedKitsPacket::encode)
                .decoder(SyncClaimedKitsPacket::decode)
                .consumer(SyncClaimedKitsPacket::handle)
                .add();
        INSTANCE.messageBuilder(OpenKitSelectorScreenPacket.class, packetId++)
                .decoder(OpenKitSelectorScreenPacket::decode)
                .encoder(OpenKitSelectorScreenPacket::encode)
                .consumer(OpenKitSelectorScreenPacket::handle)
                .add();
        INSTANCE.messageBuilder(RandomKitSelectionResultPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RandomKitSelectionResultPacket::encode)
                .decoder(RandomKitSelectionResultPacket::decode)
                .consumer(RandomKitSelectionResultPacket::handle)
                .add();
        INSTANCE.messageBuilder(RequestRandomKitPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestRandomKitPacket::decode)
                .encoder(RequestRandomKitPacket::encode)
                .consumer(RequestRandomKitPacket::handle)
                .add();

    }
}