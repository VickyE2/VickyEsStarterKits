package org.vicky.starterkits.client;

import net.minecraft.client.Minecraft;
import org.vicky.starterkits.client.gui.KitSelectionScreen;

public class ClientPacketHandlers {
    public static void openKitSelectionScreen() {
        Minecraft.getInstance().setScreen(new KitSelectionScreen());
    }
}
