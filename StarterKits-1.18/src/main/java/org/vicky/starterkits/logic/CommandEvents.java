package org.vicky.starterkits.logic;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.config.StarterKitsConfig;

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
                                    StringBuilder builder = new StringBuilder();
                                    for (var kit : kits) {
                                        builder.append("\nKit: ").append(kit.name)
                                                .append("\nDescription: ").append(kit.description)
                                                .append("\nChance in weight (1 is lowest): ").append(kit.weight)
                                                .append("\nItems: ");
                                        if (kit.items.isEmpty()) builder.append("\n  none");
                                        for (var item : kit.items) {
                                            builder.append("\n  ").append(ComponentUtil.createTranslated(item.item).getString());
                                        }
                                        builder.append("\nSlotables: ");
                                        if (kit.slotables.isEmpty()) builder.append("\n  none");
                                        for (var slota : kit.slotables) {
                                            builder.append("\n  ").append(ComponentUtil.createTranslated(slota.item).getString());
                                        }
                                        builder.append("\nPermissions: ");
                                        if (kit.requiredPermissions.isEmpty()) builder.append("\n  none");
                                        for (var perm : kit.requiredPermissions) {
                                            builder.append("\n  ").append(perm);
                                        }
                                    }
                                    ctx.getSource().sendSuccess(ComponentUtil.colorize("§aAvailable kits: " + kits.size() + builder), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("get_selector")
                                .requires(source ->
                                        source.hasPermission(2) && source.getEntity() instanceof ServerPlayer
                                )
                                .executes(ctx -> {
                                    var item = makeKitSelectorItem(
                                            ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(MOD_ID, "kit_selector")),
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