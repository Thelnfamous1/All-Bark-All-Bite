package com.infamous.all_bark_all_bite.common.registry;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.item.AdjustableInstrumentItem;
import com.infamous.all_bark_all_bite.common.item.PetWhistleItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = AllBarkAllBite.MODID)
public class ABABItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AllBarkAllBite.MODID);

    public static final RegistryObject<Item> DOG_SPAWN_EGG = ITEMS.register(String.format("%s_spawn_egg", ABABEntityTypes.DOG_NAME),
            () -> new ForgeSpawnEggItem(ABABEntityTypes.DOG, 0x523A28, 0x964B00, new Item.Properties()));

    public static final RegistryObject<Item> ILLAGER_HOUND_SPAWN_EGG = ITEMS.register(String.format("%s_spawn_egg", ABABEntityTypes.ILLAGER_HOUND_NAME),
            () -> new ForgeSpawnEggItem(ABABEntityTypes.ILLAGER_HOUND, 14144467, 13545366, new Item.Properties()));

    public static final RegistryObject<Item> HOUNDMASTER_SPAWN_EGG = ITEMS.register(String.format("%s_spawn_egg", ABABEntityTypes.HOUNDMASTER_NAME),
            () -> new ForgeSpawnEggItem(ABABEntityTypes.HOUNDMASTER, 9804699, 1973274, new Item.Properties()));

    public static final String WHISTLE_NAME = "whistle";
    public static final RegistryObject<AdjustableInstrumentItem> WHISTLE = ITEMS.register(WHISTLE_NAME,
            () -> new PetWhistleItem(new Item.Properties().stacksTo(1), ABABTags.WHISTLES));

    @SubscribeEvent
    static void onCreativeModeTab(BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey() == CreativeModeTabs.SPAWN_EGGS){
            event.accept(DOG_SPAWN_EGG);
            event.accept(HOUNDMASTER_SPAWN_EGG);
            event.accept(ILLAGER_HOUND_SPAWN_EGG);
        }
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES){
            event.accept(WHISTLE);
        }
    }
}
