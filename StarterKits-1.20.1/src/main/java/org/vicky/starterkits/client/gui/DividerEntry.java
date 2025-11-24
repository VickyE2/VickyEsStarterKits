package org.vicky.starterkits.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class DividerEntry extends ContainerObjectSelectionList.Entry<DividerEntry> {
    @Override
    public void render(PoseStack poseStack, int index, int y, int x, int width, int height,
                       int mouseX, int mouseY, boolean hovered, float partialTicks) {
        int lineY = y + height / 2;
        net.minecraft.client.gui.GuiComponent.fill(poseStack,
                x,           // start x
                lineY,       // start y
                x + width,   // end x
                lineY + 1,   // end y (1 pixel tall)
                0x44447777   // ARGB gray
        );
    }


    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return Collections.emptyList();
    }
}
