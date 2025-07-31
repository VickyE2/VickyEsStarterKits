package org.vicky.starterkits.network.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.function.Supplier;

import static org.vicky.starterkits.items.KitSelectorItem.updateLore;

public record RequestRandomKitPacket() {
    public static void encode(RequestRandomKitPacket pkt, FriendlyByteBuf buf) {}
    public static RequestRandomKitPacket decode(FriendlyByteBuf buf) {return new RequestRandomKitPacket();}

    public static void handle(RequestRandomKitPacket pkt, Supplier<NetworkEvent.Context> ctx) {
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
                        PacketHandler.INSTANCE.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                new RandomKitSelectionResultPacket(kit.name, left)
                        );
                    } else {
                        player.sendMessage(ComponentUtil.createTranslated("§cNo kits available!"), player.getUUID());
                    }
                } else {
                    player.sendMessage(ComponentUtil.createTranslated("§cNo usages left!"), player.getUUID());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
