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
import java.util.*;
import java.util.function.Supplier;

import static org.vicky.starterkits.data.KitDeserializer.parseHexColor;

public record CreateKitPacket(String kitName, @Nullable String description, List<ItemStack> kitItems, Map<ItemStack, String> slotables, List<String> perms, String texColor, String descriptionColor, int weight) {

    public static void encode(CreateKitPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.kitName);
        buf.writeUtf(pkt.description != null ? pkt.description : "");
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
        buf.writeInt(pkt.weight);
        buf.writeInt(pkt.kitItems.size());
        for (ItemStack stack : pkt.kitItems) {
            buf.writeItem(stack);
        }
        buf.writeInt(pkt.slotables.size());
        for (var stack : pkt.slotables.entrySet()) {
            buf.writeItem(stack.getKey());
            buf.writeUtf(stack.getValue());
        }
        buf.writeInt(pkt.perms.size());
        for (String stack : pkt.perms) {
            buf.writeUtf(stack);
        }
    }

    public static CreateKitPacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        String desc = buf.readUtf();
        if (desc.isEmpty()) desc = "";
        String textColor = buf.readUtf();
        String descColor = buf.readUtf();
        int weight = buf.readInt();
        int size = buf.readInt();
        List<ItemStack> items = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(buf.readItem());
        }
        int slotablesSize = buf.readInt();
        Map<ItemStack, String> slotables = new java.util.HashMap<>(slotablesSize);
        for (int i = 0; i < slotablesSize; i++) {
            slotables.put(buf.readItem(), buf.readUtf());
        }
        int permsSize = buf.readInt();
        List<String> perms = new java.util.ArrayList<>(permsSize);
        for (int i = 0; i < permsSize; i++) {
            perms.add(buf.readUtf());
        }

        return new CreateKitPacket(name, desc, items, slotables, perms, textColor, descColor, weight);
    }


    public static void handle(CreateKitPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Kit toMake = new Kit();

            toMake.name = pkt.kitName;
            toMake.description = pkt.description;
            toMake.textColor = parseHexColor(pkt.texColor);
            toMake.descriptionColor = parseHexColor(pkt.descriptionColor);
            toMake.weight = pkt.weight;
            if (toMake.items == null) toMake.items = new ArrayList<>();
            if (toMake.slotables == null) toMake.slotables = new ArrayList<>();
            if (toMake.requiredPermissions == null) toMake.requiredPermissions = new ArrayList<>();
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
            for (var slotable : pkt.slotables.entrySet()) {
                if (slotable.getKey() == null) continue;
                if (slotable.getValue().isEmpty()) continue;

                Kit.KitSlotable kitSlotable = new Kit.KitSlotable();

                var tag = slotable.getKey().getTag();
                kitSlotable.nbt = tag != null ? tag.toString() : null;
                var nullable = ForgeRegistries.ITEMS.getKey(slotable.getKey().getItem());
                kitSlotable.item = nullable != null ? nullable.toString() : "minecraft:air";
                if (slotable.getKey().getCount() > 1 && slotable.getValue().equals("offhand")) {
                    kitSlotable.item += ":" + slotable.getKey().getCount();
                }
                kitSlotable.slot = slotable.getValue();
                toMake.slotables.add(kitSlotable);
            }
            for (var perm : pkt.perms) {
                if (perm.isEmpty()) continue;
                toMake.requiredPermissions.add(perm);
            }

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Kit.class, new KitDeserializer())
                    .registerTypeAdapter(Kit.class, new KitSerializer())
                    .registerTypeAdapter(Kit.KitItem.class, new KitDeserializer.KitItemDeserializer())
                    .registerTypeAdapter(Kit.KitSlotable.class, new KitDeserializer.KitSlotableDeserializer())
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
                ctx.get().getSender().sendSystemMessage(ComponentUtil.createTranslated("§aKit Created Successfully"));
            } catch (IOException e) {
                StarterKits.LOGGER.error("Could not write kit JSON!", e);
                ctx.get().getSender().sendSystemMessage(ComponentUtil.createTranslated("§sCould not write kit JSON! " + e.getMessage()));
                e.printStackTrace();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
