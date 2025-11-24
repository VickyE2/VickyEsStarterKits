package org.vicky.starterkits.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.vicky.starterkits.client.ClientClaimedKitsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncClaimedKitsPacket {
    private final List<String> claimedKits;

    public SyncClaimedKitsPacket(List<String> claimedKits) {
        this.claimedKits = claimedKits;
    }

    public static void encode(SyncClaimedKitsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.claimedKits.size());
        for (String name : msg.claimedKits) {
            buf.writeUtf(name);
        }
    }

    public static SyncClaimedKitsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<String> claimed = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            claimed.add(buf.readUtf());
        }
        return new SyncClaimedKitsPacket(claimed);
    }

    public static void handle(SyncClaimedKitsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client-side copy
            ClientClaimedKitsManager.INSTANCE.updateClaimed(msg.claimedKits);
        });
        ctx.get().setPacketHandled(true);
    }
}
