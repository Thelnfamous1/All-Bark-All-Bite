package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.item.CyclableInstrumentItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ABABItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AllBarkAllBite.MODID);

    public static final RegistryObject<Item> DOG_SPAWN_EGG = ITEMS.register(String.format("%s_spawn_egg", ABABEntityTypes.DOG_NAME),
            () -> new ForgeSpawnEggItem(ABABEntityTypes.DOG, 0x523A28, 0x964B00, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> ILLAGER_HOUND_SPAWN_EGG = ITEMS.register(String.format("%s_spawn_egg", ABABEntityTypes.ILLAGER_HOUND_NAME),
            () -> new ForgeSpawnEggItem(ABABEntityTypes.ILLAGER_HOUND, 14144467, 13545366, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<Item> HOUNDMASTER_SPAWN_EGG = ITEMS.register(String.format("%s_spawn_egg", ABABEntityTypes.HOUNDMASTER_NAME),
            () -> new ForgeSpawnEggItem(ABABEntityTypes.HOUNDMASTER, 9804699, 1973274, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final String WHISTLE_NAME = "whistle";
    public static final RegistryObject<CyclableInstrumentItem> WHISTLE = ITEMS.register(WHISTLE_NAME,
            () -> new CyclableInstrumentItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC).stacksTo(1), ABABTags.WHISTLES_INSTRUMENT));
}
