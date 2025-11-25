package org.vicky.starterkits.client;

import net.minecraft.client.Minecraft;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.gui.KitSelectionScreen;
import org.vicky.starterkits.client.gui.RandomKitConfirmationScreen;
import org.vicky.starterkits.network.packets.RandomKitSelectionResultPacket;

public class ClientPacketHandlers {
    public static void openKitSelectionScreen() {
        Minecraft.getInstance().setScreen(new KitSelectionScreen());
    }
    public static void openRandomKitSelectionResultScreen(String kitName, int rollsLeft) {
        if (Minecraft.getInstance().player != null) {
            var kit = StarterKits.KIT_DATA.getKit(kitName);
            if (kit != null)
                Minecraft.getInstance().setScreen(new RandomKitConfirmationScreen(kit, rollsLeft));
            else
                Minecraft.getInstance().player.sendSystemMessage(ComponentUtil.createTranslated("A sever desync error has occurred. please retry"));
        }
    }
}
