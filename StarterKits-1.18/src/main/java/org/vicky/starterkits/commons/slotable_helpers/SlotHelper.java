package org.vicky.starterkits.commons.slotable_helpers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ComponentUtil;

public class SlotHelper {
    public static void giveToSlot(ServerPlayer player, String slot, ItemStack stack) {
        switch (slot.toLowerCase()) {
            case "head", "helmet" -> {
                if (!player.getInventory().armor.get(3).isEmpty()) {
                    player.addItem(player.getInventory().armor.get(3)); // give back old helmet
                }
                player.getInventory().armor.set(3, stack);
            }
            case "chest", "chestplate" -> {
                if (!player.getInventory().armor.get(2).isEmpty()) {
                    player.addItem(player.getInventory().armor.get(2)); // give back old helmet
                }
                player.getInventory().armor.set(2, stack);
            }
            case "legs", "leggings" -> {
                if (!player.getInventory().armor.get(1).isEmpty()) {
                    player.addItem(player.getInventory().armor.get(1)); // give back old helmet
                }
                player.getInventory().armor.set(1, stack);
            }
            case "feet", "boots" -> {
                if (!player.getInventory().armor.get(0).isEmpty()) {
                    player.addItem(player.getInventory().armor.get(0)); // give back old helmet
                }
                player.getInventory().armor.set(0, stack);
            }
            case "offhand" -> {
                if (!player.getInventory().offhand.get(0).isEmpty()) {
                    player.addItem(player.getInventory().offhand.get(0)); // give back old helmet
                }
                player.getInventory().offhand.set(0, stack);
            }
            case "mainhand", "hand" -> {
                if (!player.getInventory().getItem(player.getInventory().selected).isEmpty()) {
                    player.addItem(player.getInventory().getItem(player.getInventory().selected)); // give back old helmet
                }
                player.getInventory().setItem(player.getInventory().selected, stack);
            }
            default -> {
                player.sendMessage(ComponentUtil.createTranslated("§cUnknown vanilla slot '" + slot + "'"), player.getUUID());
                StarterKits.LOGGER.warn("Unknown vanilla slot '{}'", slot);
                player.addItem(stack);
            }
        }
    }

}
