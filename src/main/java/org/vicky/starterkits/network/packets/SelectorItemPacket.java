package org.vicky.starterkits.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.vicky.starterkits.client.ClientKitManager;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.logic.ClaimedKitsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record SelectorItemPacket(String name, List<String> lore) {
    public static void encode(SelectorItemPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name);
        buf.writeInt(msg.lore.size());
        for (String line : msg.lore) buf.writeUtf(line);
    }
    public static SelectorItemPacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        int size = buf.readInt();
        List<String> lore = new ArrayList<>();
        for (int i = 0; i < size; i++) lore.add(buf.readUtf());
        return new SelectorItemPacket(name, lore);
    }
    public static void handle(SelectorItemPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientKitManager.INSTANCE.setSelectorItemNameAndLore(msg.name, msg.lore);
        });
        ctx.get().setPacketHandled(true);
    }
}