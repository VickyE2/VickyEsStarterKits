package org.vicky.starterkits.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

import java.util.*;

public class KitCreationScreen extends Screen {
    private final Minecraft mc = Minecraft.getInstance();

    private ItemStack inputStack = ItemStack.EMPTY;
    private final List<ItemStack> kitItems = new ArrayList<>();
    private final List<String> perms = new ArrayList<>();
    private final Map<ItemStack, String> slotables = new HashMap<>();
    private ColorPickerWidget textColorPicker, descriptionColorPicker;

    private EditBox nameField, descField, slotNameField, permissionField, weight;
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
        this.addRenderableWidget(nameField);
        y += 20 + spacing;

        textColorPicker = new ColorPickerWidget(centerX - 25, y, (colorInt) -> {
            nameField.setTextColor(colorInt);
            return null;
        });
        this.addRenderableWidget(textColorPicker);
        y += 100 + spacing;  // adjust for color picker height

        descField = new EditBox(this.font, centerX - 100, y, 200, 20, ComponentUtil.createTranslated("Description"));
        this.addRenderableWidget(descField);

        permissionField = new EditBox(this.font, 10, y, 130, 20, ComponentUtil.createTranslated("Permission Name"));
        this.addRenderableWidget(permissionField);
        this.addRenderableWidget(Button.builder(ComponentUtil.createTranslated("+"), btn -> {
            if (!permissionField.getValue().isEmpty()) {
                perms.add(permissionField.getValue());
                permissionField.setValue("");
            }
        }).bounds(permissionField.getX() + permissionField.getWidth() + 10, permissionField.getY(), 20, 20).build());

        y += 20 + spacing;

        descriptionColorPicker = new ColorPickerWidget(centerX - 25, y, (colorInt) -> {
            descField.setTextColor(colorInt);
            return null;
        });
        this.addRenderableWidget(descriptionColorPicker);
        slotY = 4*18 + 30;

        int invX = this.width - 9*18 - 10;
        int invY = 10;

        this.addRenderableWidget(new PlayerInventoryWidget(invX, invY, clickedStack -> inputStack = clickedStack));
        slotNameField = new EditBox(this.font, invX, 4*18 + 30, 9*18 - 20 - 10 - 17 - 10, 20, ComponentUtil.createTranslated("Slot (optional slotable)"));
        slotNameField.setTextColor(0xFF888888);
        this.addRenderableWidget(slotNameField);

        this.addRenderableWidget(Button.builder(ComponentUtil.createTranslated("+"), btn -> {
            if (!inputStack.isEmpty()) {
                if (!slotNameField.getValue().isEmpty()) {
                    slotables.put(inputStack.copy(), slotNameField.getValue());
                } else {
                    kitItems.add(inputStack.copy());
                }
                inputStack = ItemStack.EMPTY;
            }
        })
      .bounds(width - 30, 4*18 + 30, 20, 20)
      .build());

        // Confirm button
        weight = new EditBox(this.font, width - 9*18, height - 60, 9*18 - 10, 20, ComponentUtil.createTranslated("Weight"));
        weight.setValue(String.valueOf(1));
        weight.setResponder(text -> {
            if (!text.matches("\\d*")) {
                weight.setValue(text.replaceAll("^\\d", ""));
            }
        });
        this.addRenderableWidget(weight);
        this.addRenderableWidget(Button.builder(ComponentUtil.createTranslated("Confirm"), btn -> {
            confirmKit();
        })
      .bounds(width - 9*18, height - 30, 9*18 - 10, 20)
      .build());
    }

    private void confirmKit() {
        String name = nameField.getValue().trim();
        String desc = descField.getValue().trim();
        if (name.isEmpty() || kitItems.isEmpty()) {
            return;
        }
        CreateKitPacket packet = new CreateKitPacket(
                name, desc, kitItems, slotables, perms, textColorPicker.getHex(), descriptionColorPicker.getHex(), Integer.parseInt(weight.getValue())
        );
        PacketHandler.INSTANCE.sendToServer(packet);
        this.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(@NotNull GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        assert mc.screen != null;

        stack.drawCenteredString(this.font, this.title, this.width / 2, this.height - 10, 0xFFFFFF);
        stack.drawString(this.font, "Kit Name:", nameField.getX(), nameField.getY() - 15, 0xFFFFFF);
        stack.drawString(this.font, "Kit Description:", descField.getX(), descField.getY() - 15, 0xFFFFFF);
        stack.drawString(this.font, "Optional Permission Name:", permissionField.getX(), permissionField.getY() - 15, 0xFFFFFF);
        stack.drawString(this.font, "Kit Random Pull Weight:", weight.getX(), weight.getY() - 15, 0xFFFFFF);
        super.render(stack, mouseX, mouseY, partialTicks);
        int slotX = width - 57;
        stack.fill(slotX, 4*18 + 30, slotX + 20, 4*18 + 30 + 20, 0x44447777); // slot border
        if (!inputStack.isEmpty()) {
            stack.renderItem(inputStack, slotX, slotY);
            if (mouseX >= slotX && mouseX <= slotX + 16 && mouseY >= slotY && mouseY <= slotY + 16) {
                stack.renderTooltip(this.font, inputStack, mouseX, mouseY);
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
        stack.fill(startX - padding, startY - padding, startX - padding + bgWidth, startY - padding + bgHeight, 0x44447777); // semi-transparent black
        for (int i = 0; i < kitItems.size(); i++) {
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            int x = startX + col * slotSize;
            int y = startY + row * slotSize;

            ItemStack s = kitItems.get(i);
            stack.renderItem(s, x, y);

            // Fix your tooltip check Y to be y
            if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
                stack.renderTooltip(this.font, s, mouseX, mouseY);
            }
        }

        startX = width - 10 - 9*18;
        startY = 4*18 + 60;
        rows = (slotables.size() + itemsPerRow - 1) / itemsPerRow;
        rows = Math.max(rows, 1);
        bgWidth = itemsPerRow * slotSize + padding * 2;
        bgHeight = rows * slotSize + padding * 2;
        stack.fill(startX - padding, startY - padding, startX - padding + bgWidth, startY - padding + bgHeight, 0x44447777); // semi-transparent black
        for (int i = 0; i < slotables.size(); i++) {
            int row = i / itemsPerRow;
            int col = i % itemsPerRow;
            int x = startX + col * slotSize;
            int y = startY + row * slotSize;

            ItemStack s = slotables.keySet().stream().toList().get(i);
            stack.renderItem(s, x, y);
            var tooltip = getTooltipFromItem(mc, s);
            tooltip.add(ComponentUtil.createTranslated("This will be placed in the " + slotables.get(s) + " slot..."));
            if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
                stack.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
            }
        }

        slotSize = 160;
        padding = 2;
        itemsPerRow = 1;
        rows = (perms.size() + itemsPerRow - 1) / itemsPerRow;
        rows = Math.max(rows, 1);
        bgWidth = itemsPerRow * slotSize + padding * 2;
        bgHeight = rows * 15 + padding * 2;
        startY = permissionField.getY() + 30;
        startX = permissionField.getX();
        stack.fill(startX - padding, startY - padding, startX - padding + bgWidth, startY - padding + bgHeight, 0x44447777); // semi-transparent black
        for (int i = 0; i < perms.size(); i++) {
            int row = i / itemsPerRow;
            int y = startY + row * slotSize;
            stack.drawString(this.font, perms.get(i),  slotX, y, 0xFFFFFFFF);
        }

    }
}
