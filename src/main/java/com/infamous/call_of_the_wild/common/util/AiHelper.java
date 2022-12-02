package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class AiHelper {
    public static List<AgeableMob> getNearbyAdults(AgeableMob ageableMob) {
        return ageableMob.getBrain().getMemory(COTWMemoryModuleTypes.NEARBY_ADULTS.get()).orElse(ImmutableList.of());
    }

    public static Optional<Player> getNearestVisibleTargetablePlayer(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
    }

    public static Optional<LivingEntity> getAngerTarget(LivingEntity livingEntity) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(livingEntity, MemoryModuleType.ANGRY_AT);
    }

    public static void stopWalking(PathfinderMob pathfinderMob) {
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        pathfinderMob.getNavigation().stop();
    }

    public static Optional<LivingEntity> getAttackTarget(LivingEntity livingEntity){
        return livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
    }

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
}
