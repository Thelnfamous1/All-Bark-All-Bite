package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class COTWMemoryModuleTypes {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, CallOfTheWild.MODID);

    public static final RegistryObject<MemoryModuleType<List<AgeableMob>>> NEARBY_ADULTS = MEMORY_MODULE_TYPES.register("nearby_adults",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<AgeableMob>>> NEAREST_VISIBLE_ADULTS = MEMORY_MODULE_TYPES.register("nearest_visible_adults",
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

    public static RegistryObject<MemoryModuleType<UUID>> OWNER = MEMORY_MODULE_TYPES.register(
            "owner",
            () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC)));

    public static RegistryObject<MemoryModuleType<BlockPos>> DIG_LOCATION = MEMORY_MODULE_TYPES.register(
            "dig_location",
            () -> new MemoryModuleType<>(Optional.empty()));
}
