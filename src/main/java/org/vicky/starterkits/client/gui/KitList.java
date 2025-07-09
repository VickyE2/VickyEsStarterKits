package org.vicky.starterkits.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.vicky.starterkits.data.Kit;

import java.util.List;

public class KitList extends ContainerObjectSelectionList<KitListEntry> {

    public KitList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
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
