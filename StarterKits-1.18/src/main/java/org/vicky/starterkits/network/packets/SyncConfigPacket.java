package org.vicky.starterkits.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.vicky.starterkits.client.ClientConfigHolder;

import java.util.List;
import java.util.function.Supplier;

public class SyncConfigPacket {
    private final String itemName;
    private final List<String> itemLore;
    private final int kitMaxUsages;
    private final boolean kitIsSelectable;
    private final boolean allowRollableKits;

    public SyncConfigPacket(String itemName, List<String> itemLore, int kitMaxUsages, boolean kitIsSelectable, boolean allowRollableKits) {
        this.itemName = itemName;
        this.itemLore = itemLore;
        this.kitMaxUsages = kitMaxUsages;
        this.kitIsSelectable = kitIsSelectable;
        this.allowRollableKits = allowRollableKits;
    }

    public static void encode(SyncConfigPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.itemName);
        buf.writeVarInt(pkt.itemLore.size());
        pkt.itemLore.forEach(buf::writeUtf);
        buf.writeInt(pkt.kitMaxUsages);
        buf.writeBoolean(pkt.kitIsSelectable);
        buf.writeBoolean(pkt.allowRollableKits);
    }

    public static SyncConfigPacket decode(FriendlyByteBuf buf) {
        String itemName = buf.readUtf();
        int loreSize = buf.readVarInt();
        List<String> lore = new java.util.ArrayList<>();
        for (int i = 0; i < loreSize; i++) {
            lore.add(buf.readUtf());
        }
        int maxUsages = buf.readInt();
        boolean isSelectable = buf.readBoolean();
        boolean allowRollableKits = buf.readBoolean();
        return new SyncConfigPacket(itemName, lore, maxUsages, isSelectable, allowRollableKits);
    }

    public static void handle(SyncConfigPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientConfigHolder.kitSelectorItemName = pkt.itemName;
                ClientConfigHolder.kitSelectorItemLore = pkt.itemLore;
                ClientConfigHolder.kitMaxUsages = pkt.kitMaxUsages;
                ClientConfigHolder.kitIsSelectable = pkt.kitIsSelectable;
                ClientConfigHolder.allowRollableKits = pkt.allowRollableKits;
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
