package org.vicky.starterkits.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * ✅ PlayerInventoryWidget: A small reusable widget to render the player's inventory grid,
 * handle clicks on slots, and show tooltips.
 * <p>
 * Usage:
 *   new PlayerInventoryWidget(x, y, onItemClicked)
 * <p>
 * - Renders 3 rows of main inventory + hot bar.
 * - Calls onItemClicked(ItemStack copy) when an item is clicked.
 * - Shows standard Minecraft tooltips.
 */
public class PlayerInventoryWidget extends AbstractWidget {

    private final Minecraft mc = Minecraft.getInstance();
    private final Consumer<ItemStack> onItemClicked;

    private final int cols = 9;
    private final int rows = 4; // 3 main + hotbar
    private final int slotSize = 18;

    public PlayerInventoryWidget(int x, int y, Consumer<ItemStack> onItemClicked) {
        super(x, y, 9 * 18, 4 * 18, Component.empty());
        this.onItemClicked = onItemClicked;
    }

    @Override
    public void render(@NotNull GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        assert mc.player != null;
        assert mc.screen != null;

        var inventory = mc.player.getInventory();
        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = (row == 3) ? col : 9 + row * 9 + col;
                ItemStack slotStack = inventory.getItem(index);

                int slotX = getX() + col * slotSize;
                int slotY = getY() + row * slotSize;

                stack.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF444444);

                if (!slotStack.is(Items.AIR)) {
                    stack.renderItem(slotStack, slotX, slotY);
                    stack.renderItemDecorations(mc.font, slotStack, slotX, slotY);
                    if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                        hoveredStack = slotStack;
                    }
                }
            }
        }

        if (!hoveredStack.isEmpty()) {
            stack.renderTooltip(mc.font, hoveredStack, mouseX, mouseY);
        }

    }

    @Override
    protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        assert mc.player != null;
        var inventory = mc.player.getInventory();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = (row == 3) ? col : 9 + row * 9 + col;

                int slotX = getX() + col * slotSize;
                int slotY = getY() + row * slotSize;

                if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                    ItemStack clicked = inventory.getItem(index);
                    if (!clicked.isEmpty() && onItemClicked != null) {
                        onItemClicked.accept(clicked.copy());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }
}
