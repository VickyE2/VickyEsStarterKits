package org.vicky.starterkits.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import org.vicky.starterkits.client.ClientConfigHolder;
import org.vicky.starterkits.client.ClientKitManager;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.config.StarterKitsConfig;
import org.vicky.starterkits.data.Kit;
import org.vicky.starterkits.network.packets.ChooseKitPacket;
import org.vicky.starterkits.network.packets.RequestRandomKitPacket;

import java.util.List;

public class KitSelectionScreen extends Screen {
    public static final boolean allowConfirmRollable = ClientConfigHolder.kitIsSelectable && ClientConfigHolder.allowRollableKits;
    private KitList kitList;

    public KitSelectionScreen() {
        super(ComponentUtil.createTranslated("Select Your Starter Kit"));
    }

    @Override
    protected void init() {
        this.kitList = new KitList(Minecraft.getInstance(), true, this.width, this.height, 40, this.height - 40, 54);
        this.kitList.setEntries(ClientKitManager.INSTANCE.getKits());
        this.addRenderableWidget(new net.minecraft.client.gui.components.Button(
                this.width - 50, this.height - 50, 100, 20,
                ComponentUtil.createTranslated("X"),
                btn -> onClose()
        ));

        this.addRenderableWidget(kitList);
        int startX = this.width / 2 - 50;
        if (allowConfirmRollable && ClientConfigHolder.kitIsSelectable) {
            startX = this.width / 2 - 100;
            this.addRenderableWidget(new net.minecraft.client.gui.components.Button(
                startX + 110, this.height - 30, 20, 20, ComponentUtil.createTranslated("↺"),
                btn -> requestRandomSelection()
            ));
        }
        this.addRenderableWidget(new net.minecraft.client.gui.components.Button(
                startX, this.height - 30, 100, 20,
                ClientConfigHolder.kitIsSelectable ? ComponentUtil.createTranslated("Confirm") : ComponentUtil.createTranslated("Get Random"),
                btn -> {
                    if (ClientConfigHolder.kitIsSelectable) {
                        confirmSelection();
                    }
                    else {
                        requestRandomSelection();
                    }
                }
        ));
    }

    private void requestRandomSelection() {
        this.onClose();
        org.vicky.starterkits.network.PacketHandler.INSTANCE.sendToServer(
                new RequestRandomKitPacket()
        );
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
                    new ChooseKitPacket(kitName, true, false)
            );
            this.onClose();
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        kitList.tick();
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        int x = this.width / 2 - 30;
        int y = this.height - 30;
        if (allowConfirmRollable && (mouseX >= x  && mouseX <= x + 20 && mouseY >= y && mouseY <= y + 20)) {
            Minecraft.getInstance().screen.renderTooltip(poseStack, List.of(ComponentUtil.colorize("§6Roll a random kit (takes 1 usage)")), java.util.Optional.empty(), mouseX, mouseY);
        }
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }
}
