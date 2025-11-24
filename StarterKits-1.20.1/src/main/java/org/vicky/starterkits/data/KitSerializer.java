package org.vicky.starterkits.data;

import com.google.gson.*;

import java.lang.reflect.Type;

import static org.vicky.starterkits.data.KitDeserializer.colorToHexString;

public class KitSerializer implements JsonSerializer<Kit> {
    @Override
    public JsonElement serialize(Kit kit, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        obj.addProperty("name", kit.name);
        obj.addProperty("description", kit.description);

        // Convert colors back to #RRGGBB hex strings
        obj.addProperty("textColor", colorToHexString(kit.textColor));
        obj.addProperty("descriptionColor", colorToHexString(kit.descriptionColor));

        obj.addProperty("weight", kit.weight);

        // Serialize items
        JsonArray itemsArray = new JsonArray();
        for (Kit.KitItem item : kit.items) {
            itemsArray.add(context.serialize(item));
        }
        obj.add("items", itemsArray);

        // Serialize items
        JsonArray slotablesArray = new JsonArray();
        for (Kit.KitSlotable slotable : kit.slotables) {
            slotablesArray.add(context.serialize(slotable));
        }
        obj.add("slotables", slotablesArray);

        // Serialize permissions
        JsonArray permissionsArray = new JsonArray();
        for (String item : kit.requiredPermissions) {
            permissionsArray.add(item);
        }
        obj.add("permissions", permissionsArray);

        return obj;
    }
}
