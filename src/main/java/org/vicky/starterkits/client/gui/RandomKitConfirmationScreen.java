package org.vicky.starterkits.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.vicky.starterkits.client.ClientKitManager;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.config.StarterKitsConfig;
import org.vicky.starterkits.data.Kit;
import org.vicky.starterkits.network.packets.ChooseKitPacket;
import org.vicky.starterkits.network.packets.RequestRandomKitPacket;

import java.util.List;

public class RandomKitConfirmationScreen extends Screen {
    private KitList kitList;
    private final Kit kit;
    private final int rollsLeft;

    public RandomKitConfirmationScreen(Kit kit, int rollsLeft) {
        super(ComponentUtil.createTranslated("Derived Kit"));
        this.kit = kit;
        this.rollsLeft = rollsLeft;
    }

    @Override
    protected void init() {
        super.init();

        this.kitList = new KitList(Minecraft.getInstance(), false, this.width, this.height, 40, this.height - 40, 54);
        this.kitList.setEntries(List.of(kit));

        this.addRenderableWidget(kitList);

        // Retry button
        var retryButton = new net.minecraft.client.gui.components.Button(
                this.width / 2 - 50, this.height - 60, 100, 20,
                ComponentUtil.createTranslated("Retries left: " + rollsLeft),
                btn -> {
                    onClose();
                    org.vicky.starterkits.network.PacketHandler.INSTANCE.sendToServer(
                            new RequestRandomKitPacket()
                    );
                }
        );
        retryButton.active = rollsLeft > 0;
        this.addRenderableWidget(retryButton);
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
        var kitName = kit.name;
        org.vicky.starterkits.network.PacketHandler.INSTANCE.sendToServer(
                new ChooseKitPacket(kitName, false, true)
        );
        this.onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
        if (rollsLeft < 1) {
            org.vicky.starterkits.network.PacketHandler.INSTANCE.sendToServer(
                    new ChooseKitPacket(kit.name, false, true)
            );
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
