package org.vicky.starterkits.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.vicky.starterkits.client.ClientClaimedKitsManager;
import org.vicky.starterkits.data.Kit;

import java.util.Collections;
import java.util.List;

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
    public void render(PoseStack poseStack, int index, int y, int x, int width, int height,
                       int mouseX, int mouseY, boolean hovered, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        int iconX = x + 30;
        int iconY = y + 30;
        if (blinkTicksRemaining > 0 && blinkOn) {
            Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_BASS, 1.0F, 0.5F);
            GuiComponent.fill(poseStack, x, y + 4, x + width + 4, y + 50, 0x88FF0000);
        } else if (list.getSelected() == this) {
            GuiComponent.fill(poseStack, x, y + (height / 2) - 10,  x + 20, y + (height / 2) + 10, 0xFF00AA00);
        }

        for (Kit.KitItem kitItem : kit.items) {
            ItemStack previewStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(kitItem.item)), kitItem.count);
            if (kitItem.nbt != null && !kitItem.nbt.isEmpty()) {
                try {
                    previewStack.setTag(net.minecraft.nbt.TagParser.parseTag(kitItem.nbt));
                } catch (Exception ignored) {}
            }
            itemRenderer.renderAndDecorateItem(previewStack, iconX, iconY);
            itemRenderer.renderGuiItemDecorations(mc.font, previewStack, iconX, iconY);
            if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                mc.screen.renderTooltip(poseStack, mc.screen.getTooltipFromItem(previewStack), java.util.Optional.empty(), mouseX, mouseY);
            }
            iconX += 18;
        }

        // Draw kit name & description
        mc.font.draw(poseStack, kit.name, x + 30, y + 10, (kit.textColor | 0xFF000000));
        mc.font.draw(poseStack, kit.description, x + 30, y + 20, (kit.descriptionColor | 0xFF000000));
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
        var player = Minecraft.getInstance().player;
        if (player != null) {
            if (ClientClaimedKitsManager.INSTANCE.isClaimed(kit.name)) {
                blinkTicksRemaining = 15;
                blinkOn = true;
            } else {
                this.select();
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
