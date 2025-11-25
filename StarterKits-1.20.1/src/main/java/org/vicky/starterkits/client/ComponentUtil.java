package org.vicky.starterkits.client;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ComponentUtil {
    public static List<String> convertToFormatted(List<Component> components) {
        List<String> strings = Lists.newArrayList();
        for (Component t : components) {
            strings.add(t.getString());
        }
        return strings;
    }

    public static MutableComponent create() {
        return createStr("");
    }

    public static MutableComponent createStr(String str) {
        return Component.literal(str);
    }
    public static Component colorize(String text) {
        // Simple version: replace & or § codes with MC format codes and parse
        // You might use TextComponent, but better to use Component.literal with formatting
        // For advanced: parse hex colors manually, or use adventure-text if you have it
        return ComponentUtil.createTranslated(applyMinecraftColorCodes(text));
    }

    private static String applyMinecraftColorCodes(String text) {
        return text.replace('&', '§'); // if you use & in config instead of §
    }

    public static MutableComponent create(MutableComponent... all) {
        MutableComponent base = create();
        for (MutableComponent tc : all) {
            base.append(tc);
        }
        return base;
    }

    public static MutableComponent createTranslated(String unlocalized) {
        return Component.translatable(unlocalized);
    }
}