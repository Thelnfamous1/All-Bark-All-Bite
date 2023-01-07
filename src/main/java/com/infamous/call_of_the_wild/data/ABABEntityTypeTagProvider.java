package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.AllBarkAllBite;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ABABEntityTypeTagProvider extends EntityTypeTagsProvider {
    public ABABEntityTypeTagProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, AllBarkAllBite.MODID, existingFileHelper);
    }

    public static ABABEntityTypeTagProvider create(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper){
        return new ABABEntityTypeTagProvider(generator, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(ABABTags.DOG_ALWAYS_HOSTILES).addTag(EntityTypeTags.SKELETONS);
        this.tag(ABABTags.DOG_HUNT_TARGETS).add(EntityType.RABBIT, EntityType.CHICKEN);
        this.tag(ABABTags.DOG_DISLIKED);

        this.tag(ABABTags.WOLF_ALWAYS_HOSTILES).addTag(EntityTypeTags.SKELETONS).add(EntityType.FOX);
        this.tag(ABABTags.WOLF_HUNT_TARGETS).add(EntityType.SHEEP, EntityType.GOAT, EntityType.PIG, EntityType.RABBIT, EntityType.CHICKEN);
        this.tag(ABABTags.WOLF_DISLIKED).add(EntityType.LLAMA).add(EntityType.TRADER_LLAMA);
    }
}
