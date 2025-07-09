package org.vicky.starterkits.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.vicky.starterkits.client.ClientKitManager;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.network.packets.ChooseKitPacket;

public class KitSelectionScreen extends Screen {
    private KitList kitList;

    public KitSelectionScreen() {
        super(ComponentUtil.createTranslated("Select Your Starter Kit"));
    }

    @Override
    protected void init() {
        this.kitList = new KitList(Minecraft.getInstance(), this.width, this.height, 40, this.height - 40, 54);
        this.kitList.setEntries(ClientKitManager.INSTANCE.getKits());
        this.addRenderableWidget(new net.minecraft.client.gui.components.Button(
                this.width - 50, this.height - 50, 100, 20,
                ComponentUtil.createTranslated("X"),
                btn -> onClose()
        ));

        this.addRenderableWidget(kitList);

        // Confirm button
        this.addRenderableWidget(new net.minecraft.client.gui.components.Button(
                this.width / 2 - 50, this.height - 30, 100, 20,
                ComponentUtil.createTranslated("Confirm"),
                btn -> confirmSelection()
        ));
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        fill(poseStack, 0, 0, this.width, this.height, 0x88000000); // ARGB with alpha=0x88
    }

    private void confirmSelection() {
        var selected = kitList.getSelected();
        if (selected != null) {
            var kitName = selected.kit.name;
            org.vicky.starterkits.network.PacketHandler.INSTANCE.sendToServer(
                    new ChooseKitPacket(kitName)
            );
            this.onClose();
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        kitList.tick();
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}
