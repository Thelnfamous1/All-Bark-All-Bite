package com.infamous.call_of_the_wild.common.ai;

import com.infamous.call_of_the_wild.common.behavior.pet.FollowOwner;
import com.infamous.call_of_the_wild.common.entity.SharedWolfAi;
import com.infamous.call_of_the_wild.common.entity.dog.DogGoalPackages;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
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
    public static void setFollowing(LivingEntity entity) {
        entity.getBrain().setMemory(ABABMemoryModuleTypes.IS_FOLLOWING.get(), Unit.INSTANCE);
    }

    public static void yieldAsPet(PathfinderMob pathfinderMob) {
        GenericAi.stopWalking(pathfinderMob);

        AiUtil.setItemPickupCooldown(pathfinderMob, DogGoalPackages.ITEM_PICKUP_COOLDOWN);

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
        yieldAsPet(tamableAnimal);
        if(tamableAnimal.canAttack(target) && tamableAnimal.wantsToAttack(target, owner)){
            StartAttacking.setAttackTarget(tamableAnimal, target);
        }
    }

    public static void commandCome(TamableAnimal tamableAnimal, LivingEntity owner, ServerLevel serverLevel) {
        tamableAnimal.setOrderedToSit(false);
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
        yieldAsPet(tamableAnimal);
        stopFollowing(tamableAnimal);
    }

    public static void commandGo(TamableAnimal tamableAnimal, HitResult hitResult) {
        tamableAnimal.setOrderedToSit(false);
        yieldAsPet(tamableAnimal);
        stopFollowing(tamableAnimal);
        if(hitResult instanceof BlockHitResult blockHitResult){
            AiUtil.setWalkAndLookTargetMemories(tamableAnimal, blockHitResult.getBlockPos(), SharedWolfAi.SPEED_MODIFIER_WALKING, 0);
        } else if(hitResult instanceof EntityHitResult entityHitResult){
            AiUtil.setWalkAndLookTargetMemories(tamableAnimal, entityHitResult.getEntity(), SharedWolfAi.SPEED_MODIFIER_WALKING, 0);
        }
    }

    public static void commandHeel(TamableAnimal tamableAnimal) {
        tamableAnimal.setOrderedToSit(false);
        yieldAsPet(tamableAnimal);
        setFollowing(tamableAnimal);
    }

    public static void commandSit(TamableAnimal tamableAnimal) {
        tamableAnimal.setOrderedToSit(true);
        tamableAnimal.setJumping(false);
        yieldAsPet(tamableAnimal);
    }

    private static void stopFollowing(LivingEntity entity) {
        entity.getBrain().eraseMemory(ABABMemoryModuleTypes.IS_FOLLOWING.get());
    }

    public static boolean isFollowing(LivingEntity entity){
        return entity.getBrain().hasMemoryValue(ABABMemoryModuleTypes.IS_FOLLOWING.get());
    }
}
