package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class COTWItemTagProvider extends ItemTagsProvider {
    public COTWItemTagProvider(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, blockTagsProvider, CallOfTheWild.MODID, existingFileHelper);
    }

    public static COTWItemTagProvider create(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper){
        return new COTWItemTagProvider(generator, blockTagsProvider, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(COTWTags.DOG_LOVED).add(Items.BONE, Items.STICK);
        this.tag(COTWTags.DOG_FOOD).add(Items.APPLE, Items.CARROT, Items.MELON_SLICE, Items.BAKED_POTATO);
    }
}
