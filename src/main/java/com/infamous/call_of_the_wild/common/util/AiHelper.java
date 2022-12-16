package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class AiHelper {
    public static List<AgeableMob> getNearbyAdults(LivingEntity ageableMob) {
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

    public static void setAvoidTarget(LivingEntity livingEntity, LivingEntity target, int avoidTimeInTicks) {
        Brain<?> brain = livingEntity.getBrain();
        brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, avoidTimeInTicks);
    }

    public static void maybeRetaliate(LivingEntity victim, List<? extends LivingEntity> allies, LivingEntity attacker, int angerTimeInTicks) {
        if (!victim.getBrain().isActive(Activity.AVOID)) {
            if (Sensor.isEntityAttackableIgnoringLineOfSight(victim, attacker)) {
                if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(victim, attacker, 4.0D)) {
                    if (attacker.getType() == EntityType.PLAYER && victim.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                        setAngerTargetToNearestTargetablePlayerIfFound(victim, attacker, angerTimeInTicks);
                        broadcastUniversalAnger(allies, angerTimeInTicks);
                    } else {
                        setAngerTarget(victim, attacker, angerTimeInTicks);
                        broadcastAngerTarget(allies, attacker, angerTimeInTicks);
                    }
                }
            }
        }
    }

    private static void setAngerTargetToNearestTargetablePlayerIfFound(LivingEntity livingEntity, LivingEntity target, int angerTimeInTicks) {
        Optional<Player> optional = getNearestVisibleTargetablePlayer(livingEntity);
        if (optional.isPresent()) {
            setAngerTarget(livingEntity, optional.get(), angerTimeInTicks);
        } else {
            setAngerTarget(livingEntity, target, angerTimeInTicks);
        }
    }

    private static void setAngerTarget(LivingEntity livingEntity, LivingEntity target, int angerTimeInTicks) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(livingEntity, target)) {
            Brain<?> brain = livingEntity.getBrain();
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            brain.setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, target.getUUID(), angerTimeInTicks);

            if (target.getType() == EntityType.PLAYER && livingEntity.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                brain.setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, angerTimeInTicks);
            }
        }
    }

    private static void broadcastUniversalAnger(List<? extends LivingEntity> allies, int angerTimeInTicks) {
        allies.forEach((adult) -> getNearestVisibleTargetablePlayer(adult).ifPresent((p) -> setAngerTarget(adult, p, angerTimeInTicks)));
    }

    public static void broadcastAngerTarget(List<? extends LivingEntity> allies, LivingEntity target, int angerTimeInTicks) {
        allies.forEach((d) -> setAngerTargetIfCloserThanCurrent(d, target, angerTimeInTicks));
    }

    private static void setAngerTargetIfCloserThanCurrent(LivingEntity livingEntity, LivingEntity target, int angerTimeInTicks) {
        Optional<LivingEntity> angerTarget = getAngerTarget(livingEntity);
        LivingEntity nearestTarget = BehaviorUtils.getNearestTarget(livingEntity, angerTarget, target);
        if (angerTarget.isEmpty() || angerTarget.get() != nearestTarget) {
            setAngerTarget(livingEntity, nearestTarget, angerTimeInTicks);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static boolean isNearAvoidTarget(LivingEntity livingEntity, int desiredDistanceFromAvoidTarget) {
        Brain<?> brain = livingEntity.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)
                && brain.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan(livingEntity, desiredDistanceFromAvoidTarget);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static boolean isNearDisliked(LivingEntity livingEntity, int desiredDistanceFromDisliked) {
        Brain<?> brain = livingEntity.getBrain();
        return brain.hasMemoryValue(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get())
                && brain.getMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get()).get().closerThan(livingEntity, desiredDistanceFromDisliked);
    }

    public static boolean hasAnyMemory(LivingEntity livingEntity, MemoryModuleType<?>... memoryModuleTypes){
        Brain<?> brain = livingEntity.getBrain();
        for(MemoryModuleType<?> memoryModuleType : memoryModuleTypes){
            if(brain.hasMemoryValue(memoryModuleType)) return true;
        }
        return false;
    }
}
