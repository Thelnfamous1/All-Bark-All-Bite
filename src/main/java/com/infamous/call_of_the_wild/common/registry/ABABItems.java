package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ABABItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AllBarkAllBite.MODID);

    public static final RegistryObject<Item> DOG_SPAWN_EGG = ITEMS.register("dog_spawn_egg",
            () -> new ForgeSpawnEggItem(ABABEntityTypes.DOG, 0x523A28, 0x964B00, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
}
