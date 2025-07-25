package org.vicky.starterkits.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.vicky.starterkits.logic.GiveSelectorMode;

import java.util.List;

public class StarterKitsConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
                .configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class CommonConfig {
        public final ForgeConfigSpec.ConfigValue<String> kitSelectorItemName;
        public final ForgeConfigSpec.ConfigValue<List<String>> kitSelectorItemLore;
        public final ForgeConfigSpec.EnumValue<GiveSelectorMode> giveSelectorMode;
        public final ForgeConfigSpec.ConfigValue<Integer> kitMaxUsages;
        public final ForgeConfigSpec.ConfigValue<Boolean> kitIsSelectable;
        public final ForgeConfigSpec.ConfigValue<Boolean> allowRollableKits;
        public final ForgeConfigSpec.ConfigValue<Boolean> breakKitSelector;
        public final ForgeConfigSpec.ConfigValue<Boolean> breakSelectorOnRandomConfirm;
        public final ForgeConfigSpec.ConfigValue<Boolean> allowInfiniteKits;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            giveSelectorMode = builder
                    .comment("When to give the selector: ALWAYS, ONCE, NONE")
                    .defineEnum("giveSelectorMode", GiveSelectorMode.ONCE);
            kitMaxUsages = builder
                    .comment("The number of times a player can use the kit selector to claim kits...")
                    .define("kitMaxUsages", 2);
            kitIsSelectable = builder
                    .comment("This sets weather or not to use randomised weighted selection for kits")
                    .define("kitIsSelectable", true);
            allowRollableKits = builder
                    .comment("Allow players to roll random kit even when not enforced")
                    .define("allowRollableKits", true);
            breakKitSelector = builder
                    .comment("Enables the ability of the selector item to break when usages exhausted")
                    .define("breakKitSelector", true);
            breakSelectorOnRandomConfirm = builder
                    .comment("Enables the ability of the selector item to break when a random roll is confirmed denying any other roll chances.")
                    .define("breakSelectorOnRandomConfirm", false);
            allowInfiniteKits = builder
                    .comment("Removes the capability of players claiming a kit once.")
                    .define("allowInfiniteKits", false);
            builder.pop();
            builder.push("SelectorItem");
            kitSelectorItemName = builder
                    .comment("The name of the Selector Item when giving it to a player")
                    .define("name", "§6Starter §eKit §7Selector");
            kitSelectorItemLore = builder
                    .comment("The lore list of the Selector Item when giving it to a player")
                    .define("lore", List.of("§7Choose your path", "§aRight-click to open kit menu... How else lol"));
            builder.pop();
        }
    }
}
