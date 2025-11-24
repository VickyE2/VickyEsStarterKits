package org.vicky.starterkits.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ColorPickerWidget extends AbstractWidget {
    private final Minecraft mc = Minecraft.getInstance();
    private final SimpleSlider redSlider, greenSlider, blueSlider;
    private SimpleSlider activeSlider = null;

    private int red = 255, green = 255, blue = 255;

    public ColorPickerWidget(int x, int y, Function<Integer, Void> onChange) {
        super(x, y, 0, 0, new TextComponent("Color Picker"));

        int sliderWidth = 120;
        int sliderHeight = 20;
        int spacing = 25;

        redSlider = new SimpleSlider(x, y, sliderWidth, sliderHeight, "Red", red / 255.0, (value) -> {
            red = (int) (value * 255);
            onChange.apply(getColorInt());
        });
        greenSlider = new SimpleSlider(x, y + spacing, sliderWidth, sliderHeight, "Green", green / 255.0, (value) -> {
            green = (int) (value * 255);
            onChange.apply(getColorInt());
        });
        blueSlider = new SimpleSlider(x, y + spacing*2, sliderWidth, sliderHeight, "Blue", blue / 255.0, (value) -> {
            blue = (int) (value * 255);
            onChange.apply(getColorInt());
        });
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        redSlider.render(poseStack, mouseX, mouseY, partialTicks);
        greenSlider.render(poseStack, mouseX, mouseY, partialTicks);
        blueSlider.render(poseStack, mouseX, mouseY, partialTicks);

        // Draw preview box under sliders
        int previewX = x + 130;
        int previewY = y;
        int previewW = 20;
        int previewH = 70;

        fill(poseStack, previewX, previewY, previewX + previewW, previewY + previewH, getColorInt());

        // Draw hex text
        String hex = getHex();
        mc.font.draw(poseStack, "Hex: " + hex, x + 5, previewY + 80, 0xFFFFFF);
    }

    private int getColorInt() {
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    public String getHex() {
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (redSlider.mouseClicked(mouseX, mouseY, button)) {
            activeSlider = redSlider;
            return true;
        }
        if (greenSlider.mouseClicked(mouseX, mouseY, button)) {
            activeSlider = greenSlider;
            return true;
        }
        if (blueSlider.mouseClicked(mouseX, mouseY, button)) {
            activeSlider = blueSlider;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (activeSlider != null) {
            return activeSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeSlider != null) {
            boolean handled = activeSlider.mouseReleased(mouseX, mouseY, button);
            activeSlider = null;
            return handled;
        }
        return false;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput p_169152_) {

    }
}
