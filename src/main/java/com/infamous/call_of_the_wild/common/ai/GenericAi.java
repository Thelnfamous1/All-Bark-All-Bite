package com.infamous.call_of_the_wild.common.ai;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.ReflectionUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class GenericAi {

    public static List<LivingEntity> getNearbyAdults(LivingEntity ageableMob) {
        return ageableMob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_ADULTS.get()).orElse(ImmutableList.of());
    }

    public static List<LivingEntity> getNearbyVisibleAdults(LivingEntity ageableMob) {
        return ageableMob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get()).orElse(ImmutableList.of());
    }

    public static List<LivingEntity> getNearbyAllies(LivingEntity ageableMob) {
        return ageableMob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_ALLIES.get()).orElse(ImmutableList.of());
    }

    public static List<LivingEntity> getNearbyVisibleAllies(LivingEntity ageableMob) {
        return ageableMob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get()).orElse(ImmutableList.of());
    }

    public static Optional<Player> getNearestVisibleTargetablePlayer(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
    }

    public static void stopWalking(PathfinderMob pathfinderMob) {
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        pathfinderMob.getNavigation().stop();
    }

    public static Optional<LivingEntity> getAttackTarget(LivingEntity livingEntity){
        return livingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
    }

    public static void setAvoidTarget(LivingEntity livingEntity, LivingEntity target, int avoidTimeInTicks) {
        AiUtil.eraseMemories(livingEntity, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.WALK_TARGET);
        livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, avoidTimeInTicks);
    }

    public static boolean isNearAvoidTarget(LivingEntity livingEntity, int desiredDistanceFromAvoidTarget) {
        return isNearTarget(livingEntity, desiredDistanceFromAvoidTarget, MemoryModuleType.AVOID_TARGET);
    }

    public static boolean isNearDisliked(LivingEntity livingEntity, int desiredDistanceFromDisliked) {
        return isNearTarget(livingEntity, desiredDistanceFromDisliked, ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static boolean isNearTarget(LivingEntity livingEntity, int closeEnough, MemoryModuleType<LivingEntity> targetMemory) {
        Brain<?> brain = livingEntity.getBrain();
        return brain.hasMemoryValue(targetMemory)
                && brain.getMemory(targetMemory).get().closerThan(livingEntity, closeEnough);
    }

    public static Vec3 getRandomNearbyPos(PathfinderMob pathfinderMob, int maxXZDistance, int maxYDistance) {
        Vec3 pos = LandRandomPos.getPos(pathfinderMob, maxXZDistance, maxYDistance);
        return pos == null ? pathfinderMob.position() : pos;
    }

    public static boolean seesPlayerHoldingWantedItem(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    public static boolean doesntSeeAnyPlayerHoldingWantedItem(LivingEntity livingEntity) {
        return !seesPlayerHoldingWantedItem(livingEntity);
    }

    public static void wakeUp(LivingEntity mob) {
        if(mob instanceof Fox fox){
            ReflectionUtil.callMethod("m_28626_", fox, false);
        }
        mob.stopSleeping();
        mob.getBrain().setMemory(MemoryModuleType.LAST_WOKEN, mob.level.getGameTime());
    }

    public static void goToSleep(LivingEntity mob) {
        if(mob instanceof Fox fox){
            ReflectionUtil.callMethod("m_28626_", fox, true);
        }
        mob.startSleeping(mob.blockPosition());
        mob.getBrain().setMemory(MemoryModuleType.LAST_SLEPT, mob.level.getGameTime());
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }

    public static NearestVisibleLivingEntities getNearestVisibleLivingEntities(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .orElse(NearestVisibleLivingEntities.empty());
    }

    public static List<LivingEntity> getNearestLivingEntities(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                .orElse(ImmutableList.of());
    }
}
