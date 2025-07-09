package org.vicky.starterkits.data;

import java.util.List;

public class Kit {
    public String name;
    public String description;
    public List<KitItem> items;
    public int textColor; // default to black
    public int descriptionColor; // default to black

    public static class KitItem {
        public String item; // e.g., "minecraft:diamond_sword"
        public int count = 1;
        public String nbt; // optional NBT data as JSON or SNBT
    }
}
