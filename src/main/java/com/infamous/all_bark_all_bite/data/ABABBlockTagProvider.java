package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ABABBlockTagProvider extends BlockTagsProvider {
    public ABABBlockTagProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, AllBarkAllBite.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ABABTags.DOG_CAN_DIG).addTag(BlockTags.DIRT).addTag(BlockTags.SAND).addTag(Tags.Blocks.GRAVEL).add(Blocks.CLAY).addTag(BlockTags.SNOW);
    }
}
