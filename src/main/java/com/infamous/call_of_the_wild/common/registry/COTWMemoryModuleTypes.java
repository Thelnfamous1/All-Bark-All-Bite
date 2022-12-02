package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;

public class COTWMemoryModuleTypes {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, CallOfTheWild.MODID);

    public static final RegistryObject<MemoryModuleType<List<AgeableMob>>> NEARBY_ADULTS = MEMORY_MODULE_TYPES.register("nearby_adults",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<List<AgeableMob>>> NEAREST_VISIBLE_ADULTS = MEMORY_MODULE_TYPES.register("nearest_visible_adults",
            () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<LivingEntity>> NEAREST_VISIBLE_DISLIKED = MEMORY_MODULE_TYPES.register("nearest_visible_disliked",
            () -> new MemoryModuleType<>(Optional.empty()));
}
