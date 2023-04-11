package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.compat.FDCompat;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ABABEntityTypeTagProvider extends EntityTypeTagsProvider {
    public ABABEntityTypeTagProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), lookupProvider, AllBarkAllBite.MODID, existingFileHelper);
    }

    public static ABABEntityTypeTagProvider create(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper){
        return new ABABEntityTypeTagProvider(generator, lookupProvider, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ABABTags.DOG_ALWAYS_HOSTILES).addTag(EntityTypeTags.SKELETONS);
        this.tag(ABABTags.DOG_HUNT_TARGETS).add(EntityType.RABBIT, EntityType.CHICKEN);
        this.tag(ABABTags.DOG_DISLIKED);

        this.tag(ABABTags.WOLF_ALWAYS_HOSTILES).addTag(EntityTypeTags.SKELETONS).add(EntityType.FOX);
        this.tag(ABABTags.WOLF_HUNT_TARGETS).add(EntityType.SHEEP, EntityType.GOAT, EntityType.PIG, EntityType.RABBIT, EntityType.CHICKEN);
        this.tag(ABABTags.WOLF_DISLIKED).add(EntityType.LLAMA).add(EntityType.TRADER_LLAMA);

        this.tag(ABABTags.ILLAGER_HOUND_ALWAYS_HOSTILES).add(EntityType.VILLAGER).add(EntityType.WANDERING_TRADER).add(EntityType.IRON_GOLEM);

        this.tag(FDCompat.DOG_FOOD_USERS).add(ABABEntityTypes.DOG.get());
    }
}
