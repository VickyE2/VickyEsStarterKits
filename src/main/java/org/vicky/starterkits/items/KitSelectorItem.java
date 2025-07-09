package org.vicky.starterkits.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.client.gui.KitCreationScreen;
import org.vicky.starterkits.client.gui.KitSelectionScreen;
import org.vicky.starterkits.network.PacketHandler;
import org.vicky.starterkits.network.packets.OpenKitSelectorScreenPacket;

import java.util.UUID;

public class KitSelectorItem extends Item {
    private static final String TAG_MAX = "MaxUses";
    private static final String TAG_LEFT = "UsesLeft";
    public KitSelectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            int left = tag.getInt(TAG_LEFT);

            if (left > 0) {
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new OpenKitSelectorScreenPacket());
            } else {
                player.sendMessage(ComponentUtil.createTranslated("§cNo usages left!"), UUID.randomUUID());
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static void updateLore(ItemStack stack, int max, int left) {
        CompoundTag displayTag = stack.getOrCreateTagElement("display");

        ListTag loreTag = displayTag.getList("Lore", net.minecraft.nbt.Tag.TAG_STRING);

        // Defensive check: remove last two if present
        if (loreTag.size() >= 2) {
            loreTag.remove(loreTag.size() - 1); // remove last
            loreTag.remove(loreTag.size() - 1); // remove second last
        }

        float percent = (left / (float) max);

        String color = "§a"; // green
        if (percent < 0.2f) color = "§c"; // red
        else if (percent < 0.6f) color = "§6"; // orange

        loreTag.add(StringTag.valueOf("{\"text\":\"§6Max Usages: " + max + "\"}"));
        loreTag.add(StringTag.valueOf("{\"text\":\"" + color + "Usages Left: " + left + "\"}"));

        displayTag.put("Lore", loreTag);
    }
}
