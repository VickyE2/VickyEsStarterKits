package org.vicky.starterkits.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.client.gui.KitSelectionScreen;
import org.vicky.starterkits.client.gui.RandomKitConfirmationScreen;
import org.vicky.starterkits.data.KitDataManager;
import org.vicky.starterkits.logic.ClaimedKitsProvider;

import java.util.function.Supplier;

import static org.vicky.starterkits.items.KitSelectorItem.updateLore;

public record RandomKitSelectionResultPacket(String kitName, int rollsLeft) {

    public static void encode(RandomKitSelectionResultPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.kitName);
        buf.writeInt(pkt.rollsLeft);
    }

    public static RandomKitSelectionResultPacket decode(FriendlyByteBuf buf) {
        return new RandomKitSelectionResultPacket(buf.readUtf(), buf.readInt());
    }

    public static void handle(RandomKitSelectionResultPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                var kit = StarterKits.KIT_DATA.getKit(pkt.kitName);
                if (kit != null)
                    Minecraft.getInstance().setScreen(new RandomKitConfirmationScreen(kit, pkt.rollsLeft));
                else
                    Minecraft.getInstance().player.sendMessage(ComponentUtil.createTranslated("A sever desync error has occurred. please retry"), Minecraft.getInstance().player.getUUID());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
