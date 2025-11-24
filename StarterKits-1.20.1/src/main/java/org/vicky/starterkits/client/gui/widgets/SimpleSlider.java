package org.vicky.starterkits.client.gui.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TextComponent;

public class SimpleSlider extends AbstractSliderButton {
    private final String label;
    private final ValueChanged onValueChanged;

    public interface ValueChanged {
        void onChange(double value);
    }

    public SimpleSlider(int x, int y, int width, int height, String label, double initialValue, ValueChanged onValueChanged) {
        super(x, y, width, height, new TextComponent(label + ": " + (int)(initialValue * 255)), initialValue);
        this.label = label;
        this.onValueChanged = onValueChanged;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(new TextComponent(label + ": " + (int)(value * 255)));
    }

    @Override
    protected void applyValue() {
        onValueChanged.onChange(value);
    }
}
