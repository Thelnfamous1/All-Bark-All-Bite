package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;

public class COTWMemoryModuleTypes {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, CallOfTheWild.MODID);

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> NEAREST_ADULTS = MEMORY_MODULE_TYPES.register("nearest_adults",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> NEAREST_VISIBLE_ADULTS = MEMORY_MODULE_TYPES.register("nearest_visible_adults",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<LivingEntity>> NEAREST_VISIBLE_DISLIKED = MEMORY_MODULE_TYPES.register("nearest_visible_disliked",
            () -> new MemoryModuleType<>(Optional.empty()));
    public static RegistryObject<MemoryModuleType<Boolean>> PLAYING_WITH_ITEM = MEMORY_MODULE_TYPES.register(
            "playing_with_item",
            () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static RegistryObject<MemoryModuleType<Integer>> TIME_TRYING_TO_REACH_PLAY_ITEM = MEMORY_MODULE_TYPES.register(
            "time_trying_to_reach_play_item",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<Boolean>> DISABLE_WALK_TO_PLAY_ITEM = MEMORY_MODULE_TYPES.register(
            "disable_walk_to_play_item",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<BlockPos>> DIG_LOCATION = MEMORY_MODULE_TYPES.register(
            "dig_location",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<LivingEntity>> NEAREST_VISIBLE_HUNTABLE = MEMORY_MODULE_TYPES.register("nearest_visible_huntable",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<Boolean>> PLAYING_DISABLED = MEMORY_MODULE_TYPES.register(
            "playing_disabled",
            () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static RegistryObject<MemoryModuleType<Boolean>> FETCHING_ITEM = MEMORY_MODULE_TYPES.register(
            "fetching_item",
            () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static RegistryObject<MemoryModuleType<Integer>> TIME_TRYING_TO_REACH_FETCH_ITEM = MEMORY_MODULE_TYPES.register(
            "time_trying_to_reach_fetch_item",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<Boolean>> DISABLE_WALK_TO_FETCH_ITEM = MEMORY_MODULE_TYPES.register(
            "disable_walk_to_fetch_item",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<Boolean>> FETCHING_DISABLED = MEMORY_MODULE_TYPES.register(
            "fetching_disabled",
            () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static RegistryObject<MemoryModuleType<Boolean>> HOWLED_RECENTLY = MEMORY_MODULE_TYPES.register(
            "howled_recently",
            () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static RegistryObject<MemoryModuleType<LivingEntity>> PACK_LEADER = MEMORY_MODULE_TYPES.register(
            "pack_leader",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<Integer>> PACK_SIZE = MEMORY_MODULE_TYPES.register(
            "pack_size",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> NEAREST_ALLIES = MEMORY_MODULE_TYPES.register("nearest_allies",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> NEAREST_VISIBLE_ALLIES = MEMORY_MODULE_TYPES.register("nearest_visible_allies",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> NEAREST_BABIES = MEMORY_MODULE_TYPES.register("nearest_babies",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> NEAREST_VISIBLE_BABIES = MEMORY_MODULE_TYPES.register("nearest_visible_babies",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<PositionTracker>> LONG_JUMP_TARGET = MEMORY_MODULE_TYPES.register("long_jump_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<Unit>> IS_ALERT = MEMORY_MODULE_TYPES.register(
            "is_alert",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static RegistryObject<MemoryModuleType<Unit>> HAS_SHELTER = MEMORY_MODULE_TYPES.register(
            "has_shelter",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static RegistryObject<MemoryModuleType<LivingEntity>> STALK_TARGET = MEMORY_MODULE_TYPES.register(
            "stalk_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<Unit>> POUNCE_DELAY = MEMORY_MODULE_TYPES.register(
            "pounce_delay",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static final RegistryObject<MemoryModuleType<LivingEntity>> NEAREST_HUNTABLE = MEMORY_MODULE_TYPES.register("nearest_huntable",
            () -> new MemoryModuleType<>(Optional.empty()));
}
