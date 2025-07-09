package org.vicky.starterkits.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ClientKitManager;
import org.vicky.starterkits.data.Kit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public record KitListPacket(List<Kit> kits) {

    public static void encode(KitListPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.kits.size());
        for (Kit kit : msg.kits) {
            buf.writeUtf(kit.name);
            buf.writeUtf(kit.description);
            buf.writeInt(kit.textColor);
            buf.writeInt(kit.descriptionColor);

            List<Kit.KitItem> items = kit.items != null ? kit.items : Collections.emptyList();
            buf.writeInt(items.size());
            for (Kit.KitItem item : items) {
                buf.writeUtf(item.item != null ? item.item : "minecraft:air");
                buf.writeInt(item.count);
                buf.writeUtf(item.nbt != null ? item.nbt : "");
            }
        }
    }

    public static KitListPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Kit> kits = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Kit kit = new Kit();
            kit.name = buf.readUtf();
            kit.description = buf.readUtf();
            kit.textColor = buf.readInt();
            kit.descriptionColor = buf.readInt();

            int itemCount = buf.readInt();
            kit.items = new ArrayList<>();
            for (int j = 0; j < itemCount; j++) {
                Kit.KitItem item = new Kit.KitItem();
                item.item = buf.readUtf();
                item.count = buf.readInt();
                String nbtStr = buf.readUtf();
                item.nbt = nbtStr.isEmpty() ? null : nbtStr;
                kit.items.add(item);
            }
            kits.add(kit);
            StarterKits.LOGGER.info("Received kit: {}, textColor=0x{}", kit.name, String.format("%08X", kit.textColor));
        }
        return new KitListPacket(kits);
    }

    public static void handle(KitListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        var kitsCopy = msg.kits != null ? msg.kits : new ArrayList<Kit>(); // <— local copy, now "safe"
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientKitManager.INSTANCE::clearKits);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientKitManager.INSTANCE.acceptKits(kitsCopy));
        });
        ctx.get().setPacketHandled(true);
    }

}
