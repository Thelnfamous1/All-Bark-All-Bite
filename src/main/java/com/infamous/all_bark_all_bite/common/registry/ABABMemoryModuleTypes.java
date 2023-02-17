package com.infamous.all_bark_all_bite.common.registry;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.util.SingleEntityManager;
import com.infamous.all_bark_all_bite.common.util.MultiEntityManager;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.common.vibration.DogVibrationListenerConfig;
import com.infamous.all_bark_all_bite.common.vibration.WolfVibrationListenerConfig;
import com.infamous.all_bark_all_bite.common.sensor.vibration.EntityVibrationListener;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
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

    public static RegistryObject<MemoryModuleType<GlobalPos>> DIG_LOCATION = MEMORY_MODULE_TYPES.register(
            "dig_location",
            () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));

    public static final RegistryObject<MemoryModuleType<LivingEntity>> NEAREST_VISIBLE_HUNTABLE = MEMORY_MODULE_TYPES.register("nearest_visible_huntable",
            () -> new MemoryModuleType<>(Optional.empty()));

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

    public static RegistryObject<MemoryModuleType<SingleEntityManager>> LEADER = MEMORY_MODULE_TYPES.register(
            "leader",
            () -> new MemoryModuleType<>(Optional.of(SingleEntityManager.codec())));

    public static RegistryObject<MemoryModuleType<MultiEntityManager>> FOLLOWERS = MEMORY_MODULE_TYPES.register(
            "followers",
            () -> new MemoryModuleType<>(Optional.of(MultiEntityManager.codec())));

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

    public static RegistryObject<MemoryModuleType<EntityVibrationListener<Wolf, WolfVibrationListenerConfig>>> WOLF_VIBRATION_LISTENER = MEMORY_MODULE_TYPES.register(
            "wolf_vibration_listener",
            () -> new MemoryModuleType<>(Optional.of(EntityVibrationListener.codec(WolfVibrationListenerConfig::new))));

    public static RegistryObject<MemoryModuleType<EntityVibrationListener<Dog, DogVibrationListenerConfig>>> DOG_VIBRATION_LISTENER = MEMORY_MODULE_TYPES.register(
            "dog_vibration_listener",
            () -> new MemoryModuleType<>(Optional.of(EntityVibrationListener.codec(DogVibrationListenerConfig::new))));

    public static RegistryObject<MemoryModuleType<GlobalPos>> HOWL_LOCATION = MEMORY_MODULE_TYPES.register(
            "howl_location",
            () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));

    public static RegistryObject<MemoryModuleType<Unit>> IS_ORDERED_TO_FOLLOW = MEMORY_MODULE_TYPES.register(
            "is_ordered_to_follow",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static RegistryObject<MemoryModuleType<Unit>> IS_SHELTERED = MEMORY_MODULE_TYPES.register(
            "is_sheltered",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static RegistryObject<MemoryModuleType<Unit>> IS_ORDERED_TO_SIT = MEMORY_MODULE_TYPES.register(
            "is_ordered_to_sit",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static RegistryObject<MemoryModuleType<Unit>> IS_LEVEL_DAY = MEMORY_MODULE_TYPES.register(
            "is_level_day",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static RegistryObject<MemoryModuleType<LivingEntity>> STALK_TARGET = MEMORY_MODULE_TYPES.register(
            "stalk_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<LivingEntity>> POUNCE_TARGET = MEMORY_MODULE_TYPES.register(
            "pounce_target",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static RegistryObject<MemoryModuleType<Unit>> IS_ORDERED_TO_HEEL = MEMORY_MODULE_TYPES.register(
            "is_ordered_to_heel",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static RegistryObject<MemoryModuleType<Integer>> POUNCE_COOLDOWN_TICKS = MEMORY_MODULE_TYPES.register(
            "pounce_cooldown_ticks",
            () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static RegistryObject<MemoryModuleType<Unit>> IS_LEVEL_NIGHT = MEMORY_MODULE_TYPES.register(
            "is_level_night",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

    public static RegistryObject<MemoryModuleType<Unit>> IS_ALERT = MEMORY_MODULE_TYPES.register(
            "is_alert",
            () -> new MemoryModuleType<>(Optional.of(Codec.unit(Unit.INSTANCE))));

}
