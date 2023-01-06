package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.common.entity.dog.ai.Dog;
import com.infamous.call_of_the_wild.common.entity.dog.vibration.DogVibrationListenerConfig;
import com.infamous.call_of_the_wild.common.entity.dog.vibration.WolfVibrationListenerConfig;
import com.infamous.call_of_the_wild.common.sensor.vibration.EntityVibrationListener;
import com.infamous.call_of_the_wild.common.util.codec.MutableSetCodec;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

public class ABABMemoryModuleTypes {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, AllBarkAllBite.MODID);

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

    public static RegistryObject<MemoryModuleType<GlobalPos>> DIG_LOCATION = MEMORY_MODULE_TYPES.register(
            "dig_location",
            () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));

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

    public static RegistryObject<MemoryModuleType<UUID>> LEADER = MEMORY_MODULE_TYPES.register(
            "leader",
            () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC)));

    public static RegistryObject<MemoryModuleType<Set<UUID>>> FOLLOWERS = MEMORY_MODULE_TYPES.register(
            "followers",
            () -> new MemoryModuleType<>(Optional.of(new MutableSetCodec<>(UUIDUtil.CODEC))));

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

    public static RegistryObject<MemoryModuleType<Unit>> IS_STALKING = MEMORY_MODULE_TYPES.register(
            "is_stalking",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<Unit>> IS_SLEEPING = MEMORY_MODULE_TYPES.register(
            "is_sleeping",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static RegistryObject<MemoryModuleType<EntityVibrationListener<Wolf, WolfVibrationListenerConfig>>> WOLF_VIBRATION_LISTENER = MEMORY_MODULE_TYPES.register(
            "wolf_vibration_listener",
            () -> new MemoryModuleType<>(Optional.of(EntityVibrationListener.codec(WolfVibrationListenerConfig::new))));

    public static RegistryObject<MemoryModuleType<EntityVibrationListener<Dog, DogVibrationListenerConfig>>> DOG_VIBRATION_LISTENER = MEMORY_MODULE_TYPES.register(
            "dog_vibration_listener",
            () -> new MemoryModuleType<>(Optional.of(EntityVibrationListener.codec(DogVibrationListenerConfig::new))));

    public static RegistryObject<MemoryModuleType<GlobalPos>> HOWL_LOCATION = MEMORY_MODULE_TYPES.register(
            "howl_location",
            () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));
}
