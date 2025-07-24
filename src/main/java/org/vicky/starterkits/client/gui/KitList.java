package org.vicky.starterkits.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.jetbrains.annotations.Nullable;
import org.vicky.starterkits.data.Kit;

import java.util.List;

public class KitList extends ContainerObjectSelectionList<KitListEntry> {
    private final boolean allowSelection;

    public KitList(Minecraft mc, boolean allowSelection, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
        this.allowSelection = allowSelection;
    }

    public void setEntries(List<Kit> kits) {
        this.clearEntries();
        for (Kit kit : kits) {
            this.addEntry(new KitListEntry(kit, this));
        }
    }

    public void tick() {
        for (KitListEntry entry : this.children()) {
            entry.tick();
        }
    }

    @Nullable
    @Override
    public KitListEntry getSelected() {
        if (allowSelection) {
            return super.getSelected();
        }
        else {
            return null;
        }
    }

    @Override
    public void setSelected(@Nullable KitListEntry p_93462_) {
        if (allowSelection) {
            super.setSelected(p_93462_);
        }
    }

    @Override
    public int getRowLeft() {
        return 20; // or e.g., 5 to leave a small margin
    }

    @Override
    public int getRowWidth() {
        return this.width; // take entire width
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width - 6; // 6 is scrollbar width offset
    }
}
