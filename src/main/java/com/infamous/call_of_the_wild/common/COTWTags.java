package com.infamous.call_of_the_wild.common;

import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class COTWTags {
    public static final TagKey<Item> DOG_BURIES = ItemTags.create(new ResourceLocation(CallOfTheWild.MODID, "dog_buries"));
    public static final TagKey<Item> DOG_FETCHES = ItemTags.create(new ResourceLocation(CallOfTheWild.MODID, "dog_fetches"));
    public static final TagKey<Item> DOG_FOOD = ItemTags.create(new ResourceLocation(CallOfTheWild.MODID, "dog_food"));
    public static final TagKey<EntityType<?>> DOG_HUNT_TARGETS = createEntityTypeTag("dog_hunt_targets");

    public static final TagKey<EntityType<?>> DOG_ALWAYS_HOSTILES = createEntityTypeTag("dog_always_hostiles");
    public static final TagKey<EntityType<?>> DOG_DISLIKED = createEntityTypeTag("dog_disliked");

    public static final TagKey<Block> DOG_CAN_DIG = BlockTags.create(new ResourceLocation(CallOfTheWild.MODID, "dog_can_dig"));

    private static TagKey<EntityType<?>> createEntityTypeTag(String path) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(CallOfTheWild.MODID, path));
    }

}
