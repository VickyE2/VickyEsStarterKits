package org.vicky.starterkits.data;

import com.google.gson.*;
import org.vicky.starterkits.StarterKits;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class KitDeserializer implements JsonDeserializer<Kit> {
    public static final boolean DEBUG = false; // I might add this to the configs instead...

    @Override
    public Kit deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (DEBUG) StarterKits.LOGGER.info("Deserializing Kit: {}", json);

        JsonObject obj = json.getAsJsonObject();
        Kit kit = new Kit();

        kit.name = obj.get("name").getAsString();
        if (DEBUG) StarterKits.LOGGER.info("Parsed name: {}", kit.name);

        kit.description = obj.get("description").getAsString();
        if (DEBUG) StarterKits.LOGGER.info("Parsed description: {}", kit.description);

        kit.weight = obj.has("weight") ? obj.get("weight").getAsInt() : 1;
        if (DEBUG) StarterKits.LOGGER.info("Parsed description: {}", kit.description);

        JsonArray itemsArray = obj.has("items") ? obj.getAsJsonArray("items") : new JsonArray();
        if (DEBUG) StarterKits.LOGGER.info("Found {} items", itemsArray.size());

        List<Kit.KitItem> items = new ArrayList<>();
        for (JsonElement el : itemsArray) {
            Kit.KitItem item = context.deserialize(el, Kit.KitItem.class);
            if (DEBUG) StarterKits.LOGGER.info("Parsed KitItem: item={}, count={}, nbt={}",
                    item.item, item.count, item.nbt);
            items.add(item);
        }
        kit.items = items;

        JsonArray slotablesArray = obj.has("slotables") ? obj.getAsJsonArray("slotables") : new JsonArray();
        if (DEBUG) StarterKits.LOGGER.info("Found {} slotables", itemsArray.size());

        List<Kit.KitSlotable> slotables = new ArrayList<>();
        for (JsonElement el : slotablesArray) {
            Kit.KitSlotable slotable = context.deserialize(el, Kit.KitSlotable.class);
            if (DEBUG) StarterKits.LOGGER.info("Parsed KitSlotable: item={}, slot={}, nbt={}",
                    slotable.item, slotable.slot, slotable.nbt);
            slotables.add(slotable);
        }
        kit.slotables = slotables;

        kit.textColor = obj.has("textColor") ? parseHexColor(obj.get("textColor").getAsString()) : 0xFFFFFFFF;
        if (DEBUG) StarterKits.LOGGER.info("Parsed textColor: 0x{}", String.format("%08X", kit.textColor));

        kit.descriptionColor = obj.has("descriptionColor") ? parseHexColor(obj.get("descriptionColor").getAsString()) : 0xFFFFFFFF;
        if (DEBUG) StarterKits.LOGGER.info("Parsed descriptionColor: 0x{}", String.format("%08X", kit.descriptionColor));

        JsonArray permissionsArray = obj.has("permissions") ? obj.getAsJsonArray("permissions") : new JsonArray();
        if (DEBUG) StarterKits.LOGGER.info("Found {} permissions", itemsArray.size());

        List<String> permissions = new ArrayList<>();
        for (JsonElement el : permissionsArray) {
            String perm = el.getAsString();
            if (DEBUG) StarterKits.LOGGER.info("Parsed Permission: name={}",
                    perm);
            permissions.add(perm);
        }
        kit.requiredPermissions = permissions;

        return kit;
    }

    public static int parseHexColor(String hex) {
        if (DEBUG) StarterKits.LOGGER.info("parseHexColor input: {}", hex);

        if (hex.startsWith("#")) hex = hex.substring(1);

        int rgb = Integer.parseInt(hex, 16);
        int colorWithAlpha = (0xFF << 24) | rgb;

        if (DEBUG) StarterKits.LOGGER.info("parseHexColor parsed: rgb=0x{}, final=0x{}",
                String.format("%06X", rgb), String.format("%08X", colorWithAlpha));

        return colorWithAlpha;
    }

    /**
     * Converts ARGB int color back to #RRGGBB string.
     * Always strips alpha (assumes it’s 0xFF).
     */
    public static String colorToHexString(int color) {
        String hex = String.format("#%06X", color & 0xFFFFFF);
        if (DEBUG) StarterKits.LOGGER.info("colorToHexString: input=0x{}, output={}",
                String.format("%08X", color), hex);
        return hex;
    }

    public static class KitItemDeserializer implements JsonDeserializer<Kit.KitItem> {
        @Override
        public Kit.KitItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Kit.KitItem kitItem = new Kit.KitItem();

            if (!obj.has("item")) throw new IllegalArgumentException("KitItem must specify an item");

            kitItem.item = obj.get("item").getAsString();
            kitItem.count = obj.has("count") ? obj.get("count").getAsInt() : 1;
            kitItem.nbt = obj.has("nbt") ? obj.get("nbt").getAsString() : null;

            if (DEBUG) StarterKits.LOGGER.info("Deserialized KitItem: item={}, count={}, nbt={}",
                    kitItem.item, kitItem.count, kitItem.nbt);

            return kitItem;
        }
    }

    public static class KitSlotableDeserializer implements JsonDeserializer<Kit.KitSlotable> {
        @Override
        public Kit.KitSlotable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Kit.KitSlotable kitSlotable = new Kit.KitSlotable();

            if (!obj.has("slot")) throw new IllegalArgumentException("KitSlotable must specify a slot");
            if (!obj.has("item")) throw new IllegalArgumentException("KitSlotable must specify an item");

            kitSlotable.item = obj.get("item").getAsString();
            kitSlotable.slot = obj.get("slot").getAsString();
            kitSlotable.nbt = obj.has("nbt") ? obj.get("nbt").getAsString() : null;

            if (DEBUG) StarterKits.LOGGER.info("Deserialized KitSlotable: item={}, slot={}, nbt={}",
                    kitSlotable.item, kitSlotable.slot, kitSlotable.nbt);

            return kitSlotable;
        }
    }
}
