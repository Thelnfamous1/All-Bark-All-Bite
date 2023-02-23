package com.infamous.all_bark_all_bite.common.ai;

import com.google.common.collect.ImmutableList;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import com.infamous.all_bark_all_bite.common.util.ReflectionUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class GenericAi {

    private static final String FOX_SET_SLEEPING = "m_28626_";

    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> List<E> getNearbyAdults(E ageableMob) {
        return (List<E>) ageableMob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_ADULTS.get()).orElse(ImmutableList.of());
    }

    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> List<E> getNearbyVisibleAdults(E ageableMob) {
        return (List<E>) ageableMob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get()).orElse(ImmutableList.of());
    }

    @SuppressWarnings("unchecked")
    public static  <E extends LivingEntity> List<E> getNearbyAllies(E ageableMob) {
        return (List<E>) ageableMob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_ALLIES.get()).orElse(ImmutableList.of());
    }

    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> List<E> getNearbyVisibleAllies(E ageableMob) {
        return (List<E>) ageableMob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get()).orElse(ImmutableList.of());
    }

    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> List<E> getNearestVisibleBabies(E ageableMob) {
        return (List<E>) ageableMob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get()).orElse(ImmutableList.of());
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

    public static void wakeUp(LivingEntity mob) {
        if(mob instanceof Fox fox){
            ReflectionUtil.callMethod(FOX_SET_SLEEPING, fox, false);
        }
        mob.stopSleeping();
        mob.getBrain().setMemory(MemoryModuleType.LAST_WOKEN, mob.level.getGameTime());
    }

    public static void goToSleep(LivingEntity mob) {
        if(mob instanceof Fox fox){
            ReflectionUtil.callMethod(FOX_SET_SLEEPING, fox, true);
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

    public static Optional<WalkTarget> getWalkTarget(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.WALK_TARGET);
    }

    public static boolean isPanicking(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
    }

    public static boolean isOnPickupCooldown(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS);
    }

    public static void broadcastAttackTarget(List<? extends Mob> alertables, LivingEntity target) {
        alertables.forEach(alertable -> setAttackTargetIfCloserThanCurrent(alertable, target));
    }

    private static void setAttackTargetIfCloserThanCurrent(Mob p_34640_, LivingEntity target) {
        Optional<LivingEntity> optional = p_34640_.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        LivingEntity livingentity = BehaviorUtils.getNearestTarget(p_34640_, optional, target);
        StartAttacking.setAttackTarget(p_34640_, livingentity);
    }

    public static Optional<Player> getTemptingPlayer(PathfinderMob mob) {
        return mob.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);
    }
}
