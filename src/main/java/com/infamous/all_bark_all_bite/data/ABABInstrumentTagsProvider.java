package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.registry.ABABInstruments;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ABABInstrumentTagsProvider extends InstrumentTagsProvider {
    public ABABInstrumentTagsProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), lookupProvider, AllBarkAllBite.MODID, existingFileHelper);
    }

    public static ABABInstrumentTagsProvider create(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper){
        return new ABABInstrumentTagsProvider(generator, lookupProvider, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ABABTags.WHISTLES)
                .add(ABABInstruments.ATTACK_WHISTLE.getKey())
                .add(ABABInstruments.COME_WHISTLE.getKey())
                .add(ABABInstruments.FOLLOW_WHISTLE.getKey())
                .add(ABABInstruments.FREE_WHISTLE.getKey())
                .add(ABABInstruments.GO_WHISTLE.getKey())
                .add(ABABInstruments.HEEL_WHISTLE.getKey())
                .add(ABABInstruments.SIT_WHISTLE.getKey());
    }
}
