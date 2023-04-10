package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.registry.ABABInstruments;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ABABInstrumentTagsProvider extends InstrumentTagsProvider {
    public ABABInstrumentTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, AllBarkAllBite.MODID, existingFileHelper);
    }

    public static ABABInstrumentTagsProvider create(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper){
        return new ABABInstrumentTagsProvider(generator, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ABABTags.WHISTLES)
                .add(ABABInstruments.ATTACK_WHISTLE.get())
                .add(ABABInstruments.COME_WHISTLE.get())
                .add(ABABInstruments.FOLLOW_WHISTLE.get())
                .add(ABABInstruments.FREE_WHISTLE.get())
                .add(ABABInstruments.GO_WHISTLE.get())
                .add(ABABInstruments.HEEL_WHISTLE.get())
                .add(ABABInstruments.SIT_WHISTLE.get());
    }
}
