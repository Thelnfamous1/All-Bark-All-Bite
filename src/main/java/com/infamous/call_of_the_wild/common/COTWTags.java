package com.infamous.call_of_the_wild.common;

import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class COTWTags {

    public static final TagKey<Item> DOG_LOVED = ItemTags.create(new ResourceLocation(CallOfTheWild.MODID, "dog_loved"));
    public static final TagKey<Item> DOG_FOOD = ItemTags.create(new ResourceLocation(CallOfTheWild.MODID, "dog_food"));
    public static final TagKey<EntityType<?>> DOG_HUNT_TARGETS = createEntityTypeTag("dog_hunt_targets");

    public static final TagKey<EntityType<?>> DOG_ALWAYS_HOSTILES = createEntityTypeTag("dog_always_hostiles");
    public static final TagKey<EntityType<?>> DOG_DISLIKED = createEntityTypeTag("dog_disliked");

    private static TagKey<EntityType<?>> createEntityTypeTag(String path) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(CallOfTheWild.MODID, path));
    }

}
