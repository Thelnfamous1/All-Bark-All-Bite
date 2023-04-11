package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = AllBarkAllBite.MODID)
public class DataEventHandler {

    @SubscribeEvent
    static void onGatherData(GatherDataEvent event){
        boolean isClient = event.includeClient();
        boolean isServer = event.includeServer();

        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(isClient, ABABLangProvider.create(generator));
        generator.addProvider(isClient, ABABItemModelProvider.create(generator, existingFileHelper));
        generator.addProvider(isClient, ABABSoundDefinitionsProvider.create(generator, existingFileHelper));

        generator.addProvider(isServer, ABABEntityTypeTagProvider.create(generator, lookupProvider, existingFileHelper));

        ABABBlockTagProvider blockTagProvider = new ABABBlockTagProvider(generator, lookupProvider, existingFileHelper);
        generator.addProvider(isServer, blockTagProvider);
        generator.addProvider(isServer, ABABInstrumentTagsProvider.create(generator, lookupProvider, existingFileHelper));
        generator.addProvider(isServer, ABABItemTagProvider.create(generator, lookupProvider, blockTagProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(isServer, ABABGameEventTagsProvider.create(generator, lookupProvider, existingFileHelper));
        generator.addProvider(isServer, ABABStructureTagsProvider.create(generator, lookupProvider, existingFileHelper));

        generator.addProvider(isServer,ABABLootTableProvider.create(generator));
    }
}
