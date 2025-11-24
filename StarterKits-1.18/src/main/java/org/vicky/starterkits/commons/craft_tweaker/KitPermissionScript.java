package org.vicky.starterkits.commons.craft_tweaker;

import net.minecraft.world.entity.player.Player;

public interface KitPermissionScript {
    boolean canUse(Player player, String permission);
}

