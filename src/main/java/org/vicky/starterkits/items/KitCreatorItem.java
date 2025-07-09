package org.vicky.starterkits.items;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.client.gui.KitCreationScreen;

import java.util.List;

public class KitCreatorItem extends Item {
    public KitCreatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> KitCreatorItem::openScreen);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen() {
        Minecraft.getInstance().setScreen(new KitCreationScreen());
    }

    // Always render glint
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    // Custom tooltip
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(ComponentUtil.createTranslated("§b✦ Opens the Starter Kit Creator ✦"));
        tooltip.add(ComponentUtil.createTranslated("§7Create & configure new kits easily..."));
        tooltip.add(ComponentUtil.createTranslated("Though.....the gui looks better on scale 2 for 1080p n 720p screens"));
    }
}
