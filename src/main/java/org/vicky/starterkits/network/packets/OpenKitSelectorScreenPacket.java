package org.vicky.starterkits.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.vicky.starterkits.client.gui.KitSelectionScreen;

import java.util.function.Supplier;

public class OpenKitSelectorScreenPacket {
    public static void encode(OpenKitSelectorScreenPacket pkt, FriendlyByteBuf buf) {}

    public static OpenKitSelectorScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenKitSelectorScreenPacket();
    }

    public static void handle(OpenKitSelectorScreenPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().setScreen(new KitSelectionScreen());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
