package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ABABStructureTagsProvider extends StructureTagsProvider {
    public ABABStructureTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, AllBarkAllBite.MODID, existingFileHelper);
    }

    public static ABABStructureTagsProvider create(DataGenerator generator, ExistingFileHelper existingFileHelper){
        return new ABABStructureTagsProvider(generator, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(ABABTags.DOGS_SPAWN_IN);
        this.tag(ABABTags.DOGS_SPAWN_AS_BLACK);
    }
}
