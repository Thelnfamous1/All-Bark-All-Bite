package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = CallOfTheWild.MODID)
public class DataEventHandler {

    @SubscribeEvent
    static void onGatherData(GatherDataEvent event){
        boolean isClient = event.includeClient();
        boolean isServer = event.includeServer();

        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(isClient, COTWLangProvider.create(generator));

        generator.addProvider(isServer, COTWEntityTypeTagProvider.create(generator, existingFileHelper));

        COTWBlockTagProvider blockTagProvider = new COTWBlockTagProvider(generator, existingFileHelper);
        generator.addProvider(isServer, blockTagProvider);
        generator.addProvider(isServer, COTWItemTagProvider.create(generator, blockTagProvider, existingFileHelper));
        generator.addProvider(isServer, COTWGameEventTagsProvider.create(generator, existingFileHelper));

        generator.addProvider(isServer, new COTWLootTableProvider(generator));
    }
}
