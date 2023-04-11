package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.registry.ABABGameEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.tags.GameEventTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ABABGameEventTagsProvider extends GameEventTagsProvider {
    public ABABGameEventTagsProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), lookupProvider, AllBarkAllBite.MODID, existingFileHelper);
    }

    public static ABABGameEventTagsProvider create(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        return new ABABGameEventTagsProvider(generator, lookupProvider, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ABABTags.DOG_CAN_LISTEN);
        this.tag(ABABTags.WOLF_CAN_LISTEN).add(ABABGameEvents.ENTITY_HOWL.get());
        this.tag(GameEventTags.VIBRATIONS).add(ABABGameEvents.ENTITY_HOWL.get());
        this.tag(GameEventTags.WARDEN_CAN_LISTEN).add(ABABGameEvents.ENTITY_HOWL.get());
    }
}
