package org.vicky.starterkits.commons.slotable_helpers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import static org.vicky.starterkits.commons.CommonsVariables.curiosLoaded;

public class CuriosHelper {
    public static void giveToSlot(ServerPlayer player, String slot, ItemStack stack) {
        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(curiosItemHandler -> {
            curiosItemHandler.getStacksHandler(slot).ifPresent(slotHandler -> {
                var formerTrinket = slotHandler.getStacks().getPreviousStackInSlot(slotHandler.getSlots() - 1);
                slotHandler.getStacks().setStackInSlot(slotHandler.getSlots() - 1, stack);
                player.addItem(formerTrinket);
            });
        });
    }

    public static boolean isCuriousSlot(String slot) {
        return curiosLoaded &&
                CuriosApi.getSlotHelper().getSlotType(slot).isPresent();
    }
}
