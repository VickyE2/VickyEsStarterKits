package org.vicky.starterkits.data;

import com.google.gson.*;
import org.vicky.starterkits.StarterKits;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class KitDeserializer implements JsonDeserializer<Kit> {
    public static final boolean DEBUG = false; // ✅ toggle debug here

    @Override
    public Kit deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (DEBUG) StarterKits.LOGGER.info("Deserializing Kit: {}", json);

        JsonObject obj = json.getAsJsonObject();
        Kit kit = new Kit();

        kit.name = obj.get("name").getAsString();
        if (DEBUG) StarterKits.LOGGER.info("Parsed name: {}", kit.name);

        kit.description = obj.get("description").getAsString();
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

        kit.textColor = obj.has("textColor") ? parseHexColor(obj.get("textColor").getAsString()) : 0xFFFFFFFF;
        if (DEBUG) StarterKits.LOGGER.info("Parsed textColor: 0x{}", String.format("%08X", kit.textColor));

        kit.descriptionColor = obj.has("descriptionColor") ? parseHexColor(obj.get("descriptionColor").getAsString()) : 0xFFFFFFFF;
        if (DEBUG) StarterKits.LOGGER.info("Parsed descriptionColor: 0x{}", String.format("%08X", kit.descriptionColor));

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
}
