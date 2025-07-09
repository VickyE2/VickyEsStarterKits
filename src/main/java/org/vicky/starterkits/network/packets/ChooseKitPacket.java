package org.vicky.starterkits.network.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.logic.ClaimedKitsProvider;

import java.util.function.Supplier;

import static org.vicky.starterkits.items.KitSelectorItem.updateLore;

public record ChooseKitPacket(String kitName) {

    public static void encode(ChooseKitPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.kitName);
    }

    public static ChooseKitPacket decode(FriendlyByteBuf buf) {
        return new ChooseKitPacket(buf.readUtf());
    }

    public static void handle(ChooseKitPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(store -> {
                    if (!store.hasClaimed(pkt.kitName)) {
                        store.claimKit(pkt.kitName);
                        org.vicky.starterkits.StarterKits.KIT_DATA.giveKitToPlayer(player, pkt.kitName);

                        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
                        CompoundTag tag = stack.getOrCreateTag();
                        int max = tag.getInt("MaxUses");
                        int left = tag.getInt("UsesLeft");

                        if (left > 0) {
                            left--;
                            stack.setDamageValue(left/max);
                            tag.putInt("UsesLeft", left);
                            updateLore(stack, max, left);

                            if (left <= 0) {
                                player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }
                    } else {
                        player.sendMessage(ComponentUtil.createTranslated("You already claimed this kit!"), player.getUUID());
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
