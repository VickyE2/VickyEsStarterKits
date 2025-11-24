package org.vicky.starterkits.commons;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class CommonFunctions {
    public static boolean hasPermission(Player player, String permission) {
        try {
            LuckPerms api = LuckPermsProvider.get();
            User user = api.getUserManager().getUser(player.getUUID());
            if (user != null) {
                return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            }
        } catch (IllegalStateException ignored) {/* Do nothing lol */}
        if (StarterKitsAPI.script != null) {
            return StarterKitsAPI.script.canUse(player, permission);
        }
        return player.hasPermissions(2);
    }
}
