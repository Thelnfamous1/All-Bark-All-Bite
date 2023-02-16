package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ABABItemTagProvider extends ItemTagsProvider {
    public ABABItemTagProvider(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, blockTagsProvider, AllBarkAllBite.MODID, existingFileHelper);
    }

    public static ABABItemTagProvider create(DataGenerator generator, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper){
        return new ABABItemTagProvider(generator, blockTagsProvider, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(ABABTags.DOG_BURIES).add(Items.BONE);
        this.tag(ABABTags.DOG_FETCHES).addTag(ABABTags.DOG_BURIES).add(Items.STICK);
        this.tag(ABABTags.DOG_FOOD).add(Items.APPLE, Items.CARROT, Items.MELON_SLICE, Items.BAKED_POTATO);

        this.tag(ABABTags.WOLF_LOVED).add(Items.BONE);
        this.tag(ABABTags.WOLF_FOOD);
        this.tag(ABABTags.HAS_WOLF_INTERACTION);
    }
}
