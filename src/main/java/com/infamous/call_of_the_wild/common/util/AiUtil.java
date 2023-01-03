package com.infamous.call_of_the_wild.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;

public class AiUtil {

    private static final String LIVING_ENTITY_JUMPING = "f_20899_";

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

    public static boolean isHostile(Mob mob, LivingEntity target, TagKey<EntityType<?>> alwaysHostiles, boolean requireLineOfSight){
        return isClose(mob, target)
                && target.getType().is(alwaysHostiles)
                && isAttackable(mob, target, requireLineOfSight);
    }

    public static boolean isClose(Mob mob, LivingEntity target) {
        double followRange = getFollowRange(mob);
        return target.distanceToSqr(mob) <= followRange * followRange;
    }

    public static boolean isHuntable(Mob mob, LivingEntity target, TagKey<EntityType<?>> huntTargets, boolean requireLineOfSight){
        return isClose(mob, target)
                && isHuntTarget(mob, target, huntTargets)
                && isAttackable(mob, target, requireLineOfSight);
    }

    private static boolean isAttackable(Mob mob, LivingEntity target, boolean requireLineOfSight){
        return requireLineOfSight ? Sensor.isEntityAttackable(mob, target) : Sensor.isEntityAttackableIgnoringLineOfSight(mob, target);
    }

    public static boolean isHuntTarget(LivingEntity mob, LivingEntity target, TagKey<EntityType<?>> huntTargets) {
        return !hasAnyMemory(mob, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.HAS_HUNTING_COOLDOWN)
                && target.getType().is(huntTargets);
    }

    public static boolean isHuntableBabyTurtle(Mob mob, LivingEntity target) {
        return isClose(mob, target)
                && !hasAnyMemory(mob, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.HAS_HUNTING_COOLDOWN)
                && target instanceof Turtle turtle
                && Turtle.BABY_ON_LAND_SELECTOR.test(turtle)
                && Sensor.isEntityAttackable(mob, turtle);
    }

    public static boolean canBeConsideredAnAlly(LivingEntity mob, LivingEntity other) {
        return (mob instanceof OwnableEntity ownable && other instanceof OwnableEntity ownableOther)
                    && ownable.getOwner() == ownableOther.getOwner()
                || mob.isAlliedTo(other)
                || (mob.getType() == other.getType() || mob.getMobType() == other.getMobType())
                    && mob.getTeam() == null && other.getTeam() == null;
    }

    @SuppressWarnings("unused")
    public static boolean isJumping(LivingEntity livingEntity) {
        return ReflectionUtil.getField(LIVING_ENTITY_JUMPING, LivingEntity.class, livingEntity);
    }

    public static void lookAtTargetIgnoreLineOfSight(LivingEntity mob, LivingEntity target) {
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, BehaviorUtils.canSee(mob, target) ? new EntityTracker(target, true) : new BlockPosTracker(target.blockPosition()));
    }
}
