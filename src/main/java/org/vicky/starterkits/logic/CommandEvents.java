package org.vicky.starterkits.logic;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.config.StarterKitsConfig;

import java.nio.file.Path;

import static org.vicky.starterkits.StarterKits.MOD_ID;
import static org.vicky.starterkits.init.DefaultInits.makeKitSelectorItem;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class CommandEvents {
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("starterkits")
                        .then(Commands.literal("reload")
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> {
                                    StarterKits.KIT_DATA.reloadKitsFromConfigFolder();
                                    ctx.getSource().sendSuccess(ComponentUtil.colorize("§aReloaded!"), true);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                    var kits = StarterKits.KIT_DATA.getAllKits();
                                    ctx.getSource().sendSuccess(ComponentUtil.colorize("§aAvailable kits: " + kits.size()), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("get_selector")
                                .requires(source ->
                                        source.hasPermission(2) && source.getEntity() instanceof ServerPlayer
                                )
                                .executes(ctx -> {
                                    var item = makeKitSelectorItem(
                                            ForgeRegistries.ITEMS.getValue(new ResourceLocation(MOD_ID, "kit_selector")),
                                            StarterKitsConfig.COMMON.kitSelectorItemName.get(),
                                            StarterKitsConfig.COMMON.kitSelectorItemLore.get(),
                                            StarterKitsConfig.COMMON.kitMaxUsages.get()
                                    );
                                    ctx.getSource().getPlayerOrException().addItem(item);
                                    return 1;
                                })
                        )
        );

    }
}