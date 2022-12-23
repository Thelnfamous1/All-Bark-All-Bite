package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.COTWTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class COTWBlockTagProvider extends BlockTagsProvider {
    public COTWBlockTagProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, CallOfTheWild.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(COTWTags.DOG_CAN_DIG).addTag(BlockTags.DIRT).addTag(BlockTags.SAND).addTag(Tags.Blocks.GRAVEL).add(Blocks.CLAY).addTag(BlockTags.SNOW);
    }
}
