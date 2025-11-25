package org.vicky.starterkits.network.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.config.StarterKitsConfig;
import org.vicky.starterkits.data.Kit;
import org.vicky.starterkits.logic.ClaimedKitsProvider;
import org.vicky.starterkits.network.PacketHandler;

import java.util.UUID;
import java.util.function.Supplier;

import static org.vicky.starterkits.items.KitSelectorItem.updateLore;

public record AutoRollForUser() {
    public static void encode(AutoRollForUser pkt, FriendlyByteBuf buf) {}
    public static AutoRollForUser decode(FriendlyByteBuf buf) {return new AutoRollForUser();}
    
    public static void handle(AutoRollForUser pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
                CompoundTag tag = stack.getOrCreateTag();
                int left = tag.getInt("UsesLeft");
                int max = tag.getInt("MaxUses");
                if (left > 0) {
                    Kit kit = StarterKits.KIT_DATA.getRandomKit(player);
                    if (kit != null) {
                        left--;
                        if (StarterKitsConfig.COMMON.breakKitSelector.get())
                            stack.setDamageValue(left / max);
                        tag.putInt("UsesLeft", left);
                        updateLore(stack, 0, left);
                        player.getCapability(ClaimedKitsProvider.CLAIMED_KITS_CAPABILITY).ifPresent(store -> {
                            store.setHasRolledOnceAndClaimed(true);
                            if (!store.hasClaimed(kit.name)) {
                                store.claimKit(kit.name);
                                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncClaimedKitsPacket(store.getClaimedKits().stream().toList()));
                                StarterKits.KIT_DATA.giveKitToPlayer(player, kit.name);
                                if (StarterKitsConfig.COMMON.breakSelectorOnRandomConfirm.get()) {
                                    stack.shrink(1);
                                }
                            } else {
                                player.sendMessage(ComponentUtil.createTranslated("You already claimed this kit!"), player.getUUID());
                            }
                        });
                    } else {
                        player.sendMessage(ComponentUtil.createTranslated("§cNo kits available!"), UUID.randomUUID());
                    }
                } else {
                    player.sendMessage(ComponentUtil.createTranslated("§cNo usages left!"), UUID.randomUUID());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
