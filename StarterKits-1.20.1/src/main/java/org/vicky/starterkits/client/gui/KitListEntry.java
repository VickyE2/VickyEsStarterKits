package org.vicky.starterkits.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.vicky.starterkits.client.ClientClaimedKitsManager;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.config.StarterKitsConfig;
import org.vicky.starterkits.data.Kit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.minecraft.client.gui.screens.Screen.getTooltipFromItem;

public class KitListEntry extends ContainerObjectSelectionList.Entry<KitListEntry> {
    public final Kit kit;
    private final KitList list;
    private int blinkTicksRemaining = 0;
    private boolean blinkOn = false;

    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public KitListEntry(Kit kit, KitList list) {
        this.kit = kit;
        this.list = list;
    }

    @Override
    public void render(GuiGraphics poseStack, int index, int y, int x, int width, int height,
                       int mouseX, int mouseY, boolean hovered, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        int iconX = x + 30;
        int iconY = y + 30;
        if (blinkTicksRemaining > 0 && blinkOn) {
            Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.0F, 0.5F);
            poseStack.fill(x - 30, y + 4, x + width + 34, y + 50, 0x88FF0000);
        } else if (list.getSelected() == this) {
            poseStack.fill(x, y + (height / 2) - 10,  x + 20, y + (height / 2) + 10, 0xFF00AA00);
        }

        if (!kit.canClaimKit(mc.player)) {
            if (hovered) {
                poseStack.renderTooltip(mc.font, List.of(ComponentUtil.colorize("§6You don't have sufficient permissions for this kit.")), java.util.Optional.empty(), mouseX, mouseY);
            }
        }
        if (ClientClaimedKitsManager.INSTANCE.isClaimed(kit.name)) {
            if (hovered) {
                poseStack.renderTooltip(mc.font, List.of(ComponentUtil.colorize("§cYou have already claimed this kit.")), java.util.Optional.empty(), mouseX, mouseY);
            }
        }

        for (Kit.KitItem kitItem : kit.items) {
            ItemStack previewStack = new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(kitItem.item)), kitItem.count);
            if (kitItem.nbt != null && !kitItem.nbt.isEmpty()) {
                try {
                    previewStack.setTag(net.minecraft.nbt.TagParser.parseTag(kitItem.nbt));
                } catch (Exception ignored) {}
            }
            poseStack.renderItem(previewStack, iconX, iconY);
            poseStack.renderItemDecorations(mc.font, previewStack, iconX, iconY);
            if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                poseStack.renderTooltip(mc.font, previewStack, mouseX, mouseY);
            }
            iconX += 18;
        }

        for (Kit.KitSlotable slotItem : kit.slotables) {
            String[] parts = slotItem.item.split(":");
            ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            int count = parts.length >= 3 ? Integer.parseInt(parts[2]) : 1;
            ItemStack previewStack = new ItemStack(ForgeRegistries.ITEMS.getValue(itemId), count);
            if (slotItem.nbt != null && !slotItem.nbt.isEmpty()) {
                try {
                    previewStack.setTag(net.minecraft.nbt.TagParser.parseTag(slotItem.nbt));
                } catch (Exception ignored) {}
            }
            poseStack.renderItem(previewStack, iconX, iconY);
            poseStack.renderItemDecorations(mc.font, previewStack, iconX, iconY);
            var kitTooltip = getTooltipFromItem(mc, previewStack);
            kitTooltip.add(ComponentUtil.createTranslated(""));
            kitTooltip.add(ComponentUtil.createTranslated("§o§bThis is equipped in the " + slotItem.slot + " slot"));
            if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                int screenWidth = mc.getWindow().getGuiScaledWidth();
                int screenHeight = mc.getWindow().getGuiScaledHeight();
                // Estimate tooltip width & height using font
                int tooltipWidth = kitTooltip.stream()
                        .mapToInt(mc.font::width)
                        .max().orElse(0) + 8; // add padding

                int tooltipHeight = kitTooltip.size() * 10; // rough estimate; depends on font line height
                // Adjust X/Y if needed
                int tooltipX = mouseX;
                int tooltipY = mouseY;

                if (tooltipX + tooltipWidth > screenWidth) {
                    tooltipX = screenWidth - tooltipWidth;
                }
                if (tooltipY + tooltipHeight > screenHeight) {
                    tooltipY = screenHeight - tooltipHeight;
                }
                // Finally render
                poseStack.renderTooltip(mc.font, kitTooltip, Optional.empty(), tooltipX, tooltipY);
            }
            iconX += 18;
        }

        // Draw kit name & description
        poseStack.drawString(mc.font, kit.name, x + 30, y + 10, (kit.textColor | 0xFF000000));
        poseStack.drawString(mc.font, kit.description, x + 30, y + 20, (kit.descriptionColor | 0xFF000000));
        // new DividerEntry().render(poseStack, index, y, x, width, height, mouseX, mouseY, hovered, partialTicks);
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<? extends NarratableEntry> narratables() {
        return Collections.emptyList();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (StarterKitsConfig.COMMON.kitIsSelectable.get()) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                if (ClientClaimedKitsManager.INSTANCE.isClaimed(kit.name)) {
                    blinkTicksRemaining = 15;
                    blinkOn = true;
                } else {
                    this.select();
                }
            }
        }
        return true;
    }


    public void tick() {
        if (blinkTicksRemaining > 0) {
            blinkTicksRemaining--;
            // Toggle blinkOn every 4 ticks (~0.2s if 20 TPS)
            if (blinkTicksRemaining % 4 == 0) {
                blinkOn = !blinkOn;
            }
        }
    }

    private void select() {
        list.setSelected(this);
    }
}
