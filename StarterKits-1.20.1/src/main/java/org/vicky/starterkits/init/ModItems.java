package org.vicky.starterkits.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.starterkits.StarterKits;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StarterKits.MOD_ID);

    public static final RegistryObject<Item> KIT_SELECTOR = ITEMS.register("kit_selector",
            () -> new org.vicky.starterkits.items.KitSelectorItem(
                    new Item.Properties().stacksTo(1)
            ));
    public static final RegistryObject<Item> KIT_CREATOR = ITEMS.register("kit_creator",
            () -> new org.vicky.starterkits.items.KitCreatorItem(
                    new Item.Properties().stacksTo(1)
            ));
}
