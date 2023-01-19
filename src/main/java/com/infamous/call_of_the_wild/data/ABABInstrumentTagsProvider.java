package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.registry.ABABInstruments;
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
    protected void addTags() {
        this.tag(ABABTags.WHISTLES_INSTRUMENT)
                .add(ABABInstruments.SIT_WHISTLE.get())
                .add(ABABInstruments.COME_WHISTLE.get())
                .add(ABABInstruments.GO_WHISTLE.get());
    }
}
