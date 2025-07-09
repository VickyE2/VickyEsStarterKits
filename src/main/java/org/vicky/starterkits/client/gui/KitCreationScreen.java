package org.vicky.starterkits.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.client.gui.widgets.ColorPickerWidget;
import org.vicky.starterkits.client.gui.widgets.PlayerInventoryWidget;
import org.vicky.starterkits.network.PacketHandler;
import org.vicky.starterkits.network.packets.CreateKitPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KitCreationScreen extends Screen {
    private final Minecraft mc = Minecraft.getInstance();

    private ItemStack inputStack = ItemStack.EMPTY;
    private final List<ItemStack> kitItems = new ArrayList<>();
    private ColorPickerWidget textColorPicker, descriptionColorPicker;

    private EditBox nameField, descField;
    private int slotY;

    public KitCreationScreen() {
        super(ComponentUtil.createTranslated("Create Starter Kit"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        int y = 20;
        int spacing = 10;

        nameField = new EditBox(this.font, centerX - 100, y, 200, 20, ComponentUtil.createTranslated("Kit Name"));
        nameField.setValue("Name of Kit: SomeVeryRandomKitName556");
        this.addRenderableWidget(nameField);
        y += 20 + spacing;

        textColorPicker = new ColorPickerWidget(centerX - 25, y, (colorInt) -> {
            nameField.setTextColor(colorInt);
            return null;
        });
        this.addRenderableWidget(textColorPicker);
        y += 100 + spacing;  // adjust for color picker height

        descField = new EditBox(this.font, centerX - 100, y, 200, 20, ComponentUtil.createTranslated("Description"));
        descField.setValue("Kit Description: SomeVeryRandomKitDescription665");
        this.addRenderableWidget(descField);
        y += 20 + spacing;

        descriptionColorPicker = new ColorPickerWidget(centerX - 25, y, (colorInt) -> {
            descField.setTextColor(colorInt);
            return null;
        });
        this.addRenderableWidget(descriptionColorPicker);
        y += 100 + spacing;
        slotY = y;

        int invX = this.width - 9*18 - 10;
        int invY = 10;

        this.addRenderableWidget(new PlayerInventoryWidget(invX, invY, clickedStack -> inputStack = clickedStack));
        this.addRenderableWidget(new Button(width - 30, 4*18 + 30, 20, 20, ComponentUtil.createTranslated("+"), btn -> {
            if (!inputStack.isEmpty()) {
                kitItems.add(inputStack.copy());
                inputStack = ItemStack.EMPTY;
            }
        }));

        // Confirm button
        this.addRenderableWidget(new Button(width - 9*18, height - 30, 9*18 - 10, 20, ComponentUtil.createTranslated("Confirm"), btn -> {
            confirmKit();
        }));
    }

    private void confirmKit() {
        String name = nameField.getValue().trim();
        String desc = descField.getValue().trim();
        if (name.isEmpty() || kitItems.isEmpty()) {
            return;
        }
        CreateKitPacket packet = new CreateKitPacket(
                name, desc, kitItems, textColorPicker.getHex(), descriptionColorPicker.getHex()
        );
        PacketHandler.INSTANCE.sendToServer(packet);
        this.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(@NotNull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        assert mc.screen != null;

        drawCenteredString(stack, this.font, this.title, this.width / 2, this.height - 10, 0xFFFFFF);
        drawString(stack, this.font, "Kit Name:", nameField.x, nameField.y - 15, 0xFFFFFF);
        drawString(stack, this.font, "Kit Description:", descField.x, descField.y - 15, 0xFFFFFF);
        super.render(stack, mouseX, mouseY, partialTicks);
        int slotX = width - 57;
        fill(stack, slotX, 4*18 + 30, slotX + 17, 4*18 + 30 + 17, 0xAA888888); // slot border
        if (!inputStack.isEmpty()) {
            this.itemRenderer.renderAndDecorateItem(inputStack, slotX, slotY);
            if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                renderTooltip(stack, mc.screen.getTooltipFromItem(inputStack), Optional.empty(), mouseX, mouseY);
            }
        }

        int slotSize = 18;
        int padding = 4;
        int itemsPerRow = 9;
        int startX = 10;
        int startY = 10;
        int rows = (kitItems.size() + itemsPerRow - 1) / itemsPerRow;
        rows = Math.max(rows, 1);
        int bgWidth = itemsPerRow * slotSize + padding * 2;
        int bgHeight = rows * slotSize + padding * 2;
        GuiComponent.fill(stack, startX - padding, startY - padding, startX - padding + bgWidth, startY - padding + bgHeight, 0xAA888888); // semi-transparent black
        for (int i = 0; i < kitItems.size(); i++) {
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            int x = startX + col * slotSize;
            int y = startY + row * slotSize;

            ItemStack s = kitItems.get(i);
            this.itemRenderer.renderAndDecorateItem(s, x, y);

            // Fix your tooltip check Y to be y
            if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
                renderTooltip(stack, mc.screen.getTooltipFromItem(s), Optional.empty(), mouseX, mouseY);
            }
        }

    }
}
