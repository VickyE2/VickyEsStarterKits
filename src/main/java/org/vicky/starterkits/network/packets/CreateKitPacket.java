package org.vicky.starterkits.network.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.vicky.starterkits.StarterKits;
import org.vicky.starterkits.client.ComponentUtil;
import org.vicky.starterkits.data.Kit;
import org.vicky.starterkits.data.KitDeserializer;
import org.vicky.starterkits.data.KitSerializer;
import org.vicky.starterkits.logic.ClaimedKitsProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.vicky.starterkits.data.KitDeserializer.parseHexColor;

public record CreateKitPacket(String kitName, @Nullable String description, List<ItemStack> kitItems, String texColor, String descriptionColor) {

    public static void encode(CreateKitPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.kitName);
        buf.writeBoolean(pkt.description != null);
        if (pkt.description != null) {
            buf.writeUtf(pkt.description);
        }
        if (pkt.texColor != null && !pkt.texColor.isEmpty()) {
            buf.writeUtf(pkt.texColor);
        } else {
            buf.writeUtf("#FFFFFF");
        }
        if (pkt.descriptionColor != null && !pkt.descriptionColor.isEmpty()) {
            buf.writeUtf(pkt.descriptionColor);
        } else {
            buf.writeUtf("#FFFFFF");
        }
        buf.writeInt(pkt.kitItems.size());
        for (ItemStack stack : pkt.kitItems) {
            buf.writeItem(stack);
        }
    }

    public static CreateKitPacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();

        String desc = null;
        if (buf.readBoolean()) {
            desc = buf.readUtf();
        }
        String textColor = buf.readUtf();
        String descColor = buf.readUtf();
        int size = buf.readInt();
        List<ItemStack> items = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }

        return new CreateKitPacket(name, desc, items, textColor, descColor);
    }


    public static void handle(CreateKitPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Kit toMake = new Kit();

            toMake.name = pkt.kitName;
            toMake.description = pkt.description;
            toMake.textColor = parseHexColor(pkt.texColor);
            toMake.descriptionColor = parseHexColor(pkt.descriptionColor);
            toMake.items = new ArrayList<>();
            for (var item : pkt.kitItems) {
                if (item == null) continue;

                Kit.KitItem kitItem = new Kit.KitItem();

                var tag = item.getTag();
                kitItem.nbt = tag != null ? tag.toString() : null;
                kitItem.count = item.getCount();
                var nullable = ForgeRegistries.ITEMS.getKey(item.getItem());
                kitItem.item = nullable != null ? nullable.toString() : "minecraft:air";

                toMake.items.add(kitItem);
            }

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Kit.class, new KitDeserializer())
                    .registerTypeAdapter(Kit.class, new KitSerializer())
                    .registerTypeAdapter(Kit.KitItem.class, new KitDeserializer.KitItemDeserializer())
                    .setPrettyPrinting()
                    .create();

            var jsonKit = gson.toJson(toMake, Kit.class);
            Path kitsFolder = Paths.get(FMLPaths.CONFIGDIR.get().toString(), "starterkits", "kits");
            File jsonKitFile = new File(kitsFolder.toString(), pkt.kitName + "_generated.json");
            if (!kitsFolder.toFile().exists()) {
                kitsFolder.toFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(jsonKitFile)) {
                writer.write(jsonKit);
            } catch (IOException e) {
                StarterKits.LOGGER.error("Could not write kit JSON!", e);
                e.printStackTrace();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
