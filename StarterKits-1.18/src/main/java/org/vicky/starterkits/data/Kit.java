package org.vicky.starterkits.data;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.vicky.starterkits.commons.CommonFunctions;

import java.util.ArrayList;
import java.util.List;

public class Kit {
    public String name;
    public String description;
    public List<KitItem> items;
    public int textColor; // defaults to black since null int is 0...
    public int descriptionColor;
    public int weight;
    public List<String> requiredPermissions;
    public List<KitSlotable> slotables;

    public static class KitItem {
        public String item;
        public int count = 1;
        public String nbt;
    }

    public static class KitSlotable {
        public String slot;  // e.g. "ring"
        public String item;
        public String nbt;
    }

    public boolean canClaimKit(Player player) {
        if (requiredPermissions != null && !requiredPermissions.isEmpty()) {
            for (var perm : requiredPermissions) {
                if (!player.hasPermissions(4) && CommonFunctions.hasPermission(player, perm)) {
                    return false;
                }
            }
        }
        return true;
    }
}
