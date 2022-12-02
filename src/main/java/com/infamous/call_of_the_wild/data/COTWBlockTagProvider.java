package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class COTWBlockTagProvider extends BlockTagsProvider {
    public COTWBlockTagProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, CallOfTheWild.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
    }
}
