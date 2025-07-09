package org.vicky.starterkits.network.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.logic.ClaimedKitsProvider;
import org.vicky.starterkits.network.PacketHandler;

import java.util.function.Supplier;

import static org.vicky.starterkits.items.KitSelectorItem.updateLore;

public record ChooseKitPacket(String kitName, boolean shouldDegrade) {

    public static void encode(ChooseKitPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.kitName);
        buf.writeBoolean(pkt.shouldDegrade());
    }

    public static ChooseKitPacket decode(FriendlyByteBuf buf) {
        return new ChooseKitPacket(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(ChooseKitPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(store -> {
                    if (!store.hasClaimed(pkt.kitName)) {
                        store.claimKit(pkt.kitName);
                        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncClaimedKitsPacket(store.getClaimedKits().stream().toList()));
                        org.vicky.starterkits.StarterKits.KIT_DATA.giveKitToPlayer(player, pkt.kitName);
                        if (pkt.shouldDegrade) {
                            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
                            CompoundTag tag = stack.getOrCreateTag();
                            int max = tag.getInt("MaxUses");
                            int left = tag.getInt("UsesLeft");

                            if (left > 0) {
                                left--;
                                stack.setDamageValue(left / max);
                                tag.putInt("UsesLeft", left);
                                updateLore(stack, max, left);

                                if (left <= 0) {
                                    player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                }
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
