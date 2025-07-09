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

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            giveSelectorMode = builder
                    .comment("When to give the selector: ALWAYS, ONCE, NONE")
                    .defineEnum("giveSelectorMode", GiveSelectorMode.ONCE);
            kitMaxUsages = builder
                    .comment("The number of times a player can use the kit selector to claim kits...")
                    .define("kitMaxUsages", 2);
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
