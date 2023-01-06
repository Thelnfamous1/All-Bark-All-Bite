package com.infamous.call_of_the_wild.common;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;

public class ABABTags {
    public static final TagKey<Item> DOG_BURIES = ItemTags.create(new ResourceLocation(AllBarkAllBite.MODID, "dog_buries"));
    public static final TagKey<Item> DOG_FETCHES = ItemTags.create(new ResourceLocation(AllBarkAllBite.MODID, "dog_fetches"));
    public static final TagKey<Item> DOG_FOOD = ItemTags.create(new ResourceLocation(AllBarkAllBite.MODID, "dog_food"));
    public static final TagKey<EntityType<?>> DOG_HUNT_TARGETS = createEntityTypeTag("dog_hunt_targets");

    public static final TagKey<EntityType<?>> DOG_ALWAYS_HOSTILES = createEntityTypeTag("dog_always_hostiles");
    public static final TagKey<EntityType<?>> DOG_DISLIKED = createEntityTypeTag("dog_disliked");

    public static final TagKey<Block> DOG_CAN_DIG = BlockTags.create(new ResourceLocation(AllBarkAllBite.MODID, "dog_can_dig"));


    public static final TagKey<Item> WOLF_LOVED = ItemTags.create(new ResourceLocation(AllBarkAllBite.MODID, "wolf_loved"));
    public static final TagKey<Item> WOLF_FOOD = ItemTags.create(new ResourceLocation(AllBarkAllBite.MODID, "wolf_food"));
    public static final TagKey<EntityType<?>> WOLF_HUNT_TARGETS = createEntityTypeTag("wolf_hunt_targets");

    public static final TagKey<EntityType<?>> WOLF_ALWAYS_HOSTILES = createEntityTypeTag("wolf_always_hostiles");
    public static final TagKey<EntityType<?>> WOLF_DISLIKED = createEntityTypeTag("wolf_disliked");

    public static final TagKey<GameEvent> WOLF_CAN_LISTEN = createGameEventTag("wolf_can_listen");

    public static final TagKey<GameEvent> DOG_CAN_LISTEN = createGameEventTag("dog_can_listen");

    private static TagKey<EntityType<?>> createEntityTypeTag(String path) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(AllBarkAllBite.MODID, path));
    }
    private static TagKey<GameEvent> createGameEventTag(String path) {
        return TagKey.create(Registry.GAME_EVENT_REGISTRY, new ResourceLocation(AllBarkAllBite.MODID, path));
    }

}
