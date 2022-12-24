package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class COTWEntityTypeTagProvider extends EntityTypeTagsProvider {
    public COTWEntityTypeTagProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, CallOfTheWild.MODID, existingFileHelper);
    }

    public static COTWEntityTypeTagProvider create(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper){
        return new COTWEntityTypeTagProvider(generator, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(COTWTags.DOG_ALWAYS_HOSTILES).addTag(EntityTypeTags.SKELETONS);
        this.tag(COTWTags.DOG_HUNT_TARGETS).add(EntityType.FOX, EntityType.RABBIT, EntityType.CHICKEN);
        this.tag(COTWTags.DOG_DISLIKED);

        this.tag(COTWTags.WOLF_ALWAYS_HOSTILES).addTag(EntityTypeTags.SKELETONS);
        this.tag(COTWTags.WOLF_HUNT_TARGETS).add(EntityType.SHEEP, EntityType.GOAT, EntityType.PIG, EntityType.FOX, EntityType.RABBIT, EntityType.CHICKEN);
        this.tag(COTWTags.WOLF_DISLIKED);
    }
}
