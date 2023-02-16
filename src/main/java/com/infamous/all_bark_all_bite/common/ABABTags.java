package com.infamous.all_bark_all_bite.common;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ABABTags {
    public static final TagKey<Item> DOG_BURIES = createItemTag("dog_buries");

    public static final TagKey<Item> DOG_FETCHES = createItemTag("dog_fetches");
    public static final TagKey<Item> DOG_FOOD = createItemTag("dog_food");
    public static final TagKey<EntityType<?>> DOG_HUNT_TARGETS = createEntityTypeTag("dog_hunt_targets");
    public static final TagKey<EntityType<?>> DOG_ALWAYS_HOSTILES = createEntityTypeTag("dog_always_hostiles");
    public static final TagKey<EntityType<?>> DOG_DISLIKED = createEntityTypeTag("dog_disliked");
    public static final TagKey<Block> DOG_CAN_DIG = BlockTags.create(new ResourceLocation(AllBarkAllBite.MODID, "dog_can_dig"));
    public static final TagKey<Structure> DOGS_SPAWN_IN = createStructureTag("dogs_spawn_in");
    public static final TagKey<Structure> DOGS_SPAWN_AS_BLACK = createStructureTag("dogs_spawn_as_black");
    public static final TagKey<Item> WOLF_LOVED = createItemTag("wolf_loved");
    public static final TagKey<Item> WOLF_FOOD = createItemTag("wolf_food");
    public static final TagKey<EntityType<?>> WOLF_HUNT_TARGETS = createEntityTypeTag("wolf_hunt_targets");
    public static final TagKey<EntityType<?>> WOLF_ALWAYS_HOSTILES = createEntityTypeTag("wolf_always_hostiles");
    public static final TagKey<EntityType<?>> WOLF_DISLIKED = createEntityTypeTag("wolf_disliked");
    public static final TagKey<GameEvent> WOLF_CAN_LISTEN = createGameEventTag("wolf_can_listen");
    public static final TagKey<GameEvent> DOG_CAN_LISTEN = createGameEventTag("dog_can_listen");
    public static final TagKey<EntityType<?>> ILLAGER_HOUND_ALWAYS_HOSTILES = createEntityTypeTag(String.format("%s_always_hostiles", ABABEntityTypes.ILLAGER_HOUND_NAME));
    public static final TagKey<Instrument> WHISTLES = createInstrumentTag("whistles");
    public static final TagKey<Item> HAS_WOLF_INTERACTION = createItemTag("has_wolf_interaction");

    private static TagKey<Item> createItemTag(String path) {
        return ItemTags.create(new ResourceLocation(AllBarkAllBite.MODID, path));
    }

    private static TagKey<EntityType<?>> createEntityTypeTag(String path) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(AllBarkAllBite.MODID, path));
    }

    private static TagKey<GameEvent> createGameEventTag(String path) {
        return TagKey.create(Registry.GAME_EVENT_REGISTRY, new ResourceLocation(AllBarkAllBite.MODID, path));
    }

    private static TagKey<Instrument> createInstrumentTag(String path) {
        return TagKey.create(Registry.INSTRUMENT_REGISTRY, new ResourceLocation(AllBarkAllBite.MODID, path));
    }

    private static TagKey<Structure> createStructureTag(String path) {
        return TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(AllBarkAllBite.MODID, path));
    }
}
