package com.infamous.all_bark_all_bite.common.ai;

import com.infamous.all_bark_all_bite.common.behavior.pet.FollowOwner;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class CommandAi {

    public static void yieldAsPet(PathfinderMob pathfinderMob) {
        GenericAi.stopWalking(pathfinderMob);

        AiUtil.setItemPickupCooldown(pathfinderMob, SharedWolfAi.ITEM_PICKUP_COOLDOWN);

        AiUtil.eraseMemories(pathfinderMob,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.ANGRY_AT,
                MemoryModuleType.UNIVERSAL_ANGER,
                MemoryModuleType.AVOID_TARGET,
                ABABMemoryModuleTypes.FETCHING_ITEM.get(),
                ABABMemoryModuleTypes.DIG_LOCATION.get());
    }

    public static void commandAttack(TamableAnimal tamableAnimal, LivingEntity target, LivingEntity owner) {
        tamableAnimal.setOrderedToSit(false);
        SharedWolfAi.clearStates(tamableAnimal, true);
        yieldAsPet(tamableAnimal);
        if(tamableAnimal.canAttack(target) && tamableAnimal.wantsToAttack(target, owner)){
            StartAttacking.setAttackTarget(tamableAnimal, target);
        }
    }

    public static void commandCome(TamableAnimal tamableAnimal, LivingEntity owner, ServerLevel serverLevel) {
        tamableAnimal.setOrderedToSit(false);
        SharedWolfAi.clearStates(tamableAnimal, true);
        yieldAsPet(tamableAnimal);
        Path path = tamableAnimal.getNavigation().createPath(owner, 0);
        if(path != null && path.canReach()){
            AiUtil.setWalkAndLookTargetMemories(tamableAnimal, owner, SharedWolfAi.SPEED_MODIFIER_WALKING, 0);
        } else{
            FollowOwner.teleportToEntity(owner, serverLevel, tamableAnimal, false);
        }
    }

    public static void commandFree(TamableAnimal tamableAnimal) {
        tamableAnimal.setOrderedToSit(false);
        SharedWolfAi.clearStates(tamableAnimal, true);
        yieldAsPet(tamableAnimal);
        stopFollowing(tamableAnimal);
        stopHeeling(tamableAnimal);
    }

    public static void commandFollow(TamableAnimal tamableAnimal) {
        tamableAnimal.setOrderedToSit(false);
        SharedWolfAi.clearStates(tamableAnimal, true);
        yieldAsPet(tamableAnimal);
        stopHeeling(tamableAnimal);
        setFollowing(tamableAnimal);
    }

    public static void commandGo(TamableAnimal tamableAnimal, HitResult hitResult) {
        tamableAnimal.setOrderedToSit(false);
        SharedWolfAi.clearStates(tamableAnimal, true);
        yieldAsPet(tamableAnimal);
        stopFollowing(tamableAnimal);
        stopHeeling(tamableAnimal);
        if(hitResult instanceof BlockHitResult blockHitResult){
            AiUtil.setWalkAndLookTargetMemories(tamableAnimal, blockHitResult.getBlockPos(), SharedWolfAi.SPEED_MODIFIER_WALKING, 0);
        } else if(hitResult instanceof EntityHitResult entityHitResult){
            AiUtil.setWalkAndLookTargetMemories(tamableAnimal, entityHitResult.getEntity(), SharedWolfAi.SPEED_MODIFIER_WALKING, 0);
        }
    }

    public static void commandHeel(TamableAnimal tamableAnimal) {
        tamableAnimal.setOrderedToSit(false);
        SharedWolfAi.clearStates(tamableAnimal, true);
        yieldAsPet(tamableAnimal);
        stopFollowing(tamableAnimal);
        setHeeling(tamableAnimal);
    }

    public static void commandSit(TamableAnimal tamableAnimal) {
        tamableAnimal.setOrderedToSit(true);
        SharedWolfAi.clearStates(tamableAnimal, false);
        tamableAnimal.setJumping(false);
        yieldAsPet(tamableAnimal);
    }

    public static void setFollowing(LivingEntity entity) {
        entity.getBrain().setMemory(ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get(), Unit.INSTANCE);
    }

    private static void stopFollowing(LivingEntity entity) {
        entity.getBrain().eraseMemory(ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get());
    }

    public static boolean isFollowing(LivingEntity entity){
        return entity.getBrain().hasMemoryValue(ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get());
    }

    public static void setHeeling(LivingEntity entity) {
        entity.getBrain().setMemory(ABABMemoryModuleTypes.IS_ORDERED_TO_HEEL.get(), Unit.INSTANCE);
    }

    public static void stopHeeling(LivingEntity entity) {
        entity.getBrain().eraseMemory(ABABMemoryModuleTypes.IS_ORDERED_TO_HEEL.get());
    }

    public static boolean isHeeling(LivingEntity entity){
        return entity.getBrain().hasMemoryValue(ABABMemoryModuleTypes.IS_ORDERED_TO_HEEL.get());
    }
}
