package com.infamous.call_of_the_wild.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;

public class AiUtil {
    public static int reducedTickDelay(int ticks) {
        return Mth.positiveCeilDiv(ticks, 2);
    }

    public static double getFollowRange(Mob mob) {
        return mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    public static void addEatEffect(LivingEntity eater, Level level, FoodProperties foodProperties) {
        for(Pair<MobEffectInstance, Float> pair : foodProperties.getEffects()) {
            if (!level.isClientSide && pair.getFirst() != null && level.random.nextFloat() < pair.getSecond()) {
                eater.addEffect(new MobEffectInstance(pair.getFirst()));
            }
        }
    }

    public static boolean hasAnyMemory(LivingEntity livingEntity, MemoryModuleType<?>... memoryModuleTypes){
        Brain<?> brain = livingEntity.getBrain();
        for(MemoryModuleType<?> memoryModuleType : memoryModuleTypes){
            if(brain.hasMemoryValue(memoryModuleType)) return true;
        }
        return false;
    }

    public static void eraseAllMemories(LivingEntity livingEntity, MemoryModuleType<?>... memoryModuleTypes) {
        Brain<?> brain = livingEntity.getBrain();
        for (MemoryModuleType<?> memoryModuleType : memoryModuleTypes) {
            brain.eraseMemory(memoryModuleType);
        }
    }
}
