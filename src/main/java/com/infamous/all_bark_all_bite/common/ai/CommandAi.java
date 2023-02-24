package com.infamous.all_bark_all_bite.common.ai;

import com.infamous.all_bark_all_bite.common.behavior.pet.FollowOwner;
import com.infamous.all_bark_all_bite.common.entity.LookTargetAccessor;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.entity.WalkTargetAccessor;
import com.infamous.all_bark_all_bite.common.registry.ABABActivities;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import com.infamous.all_bark_all_bite.common.util.CompatUtil;
import com.infamous.all_bark_all_bite.common.util.DICompat;
import com.infamous.all_bark_all_bite.common.util.ReflectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class CommandAi {

    private static final String FOX_CLEAR_STATES = "m_28569_";

    public static void yieldAsPet(PathfinderMob pathfinderMob){
        yieldAsPet(pathfinderMob, Activity.IDLE);
    }

    public static void yieldAsPet(PathfinderMob pathfinderMob, Activity activity) {
        pathfinderMob.getBrain().setActiveActivityIfPossible(activity);

        AiUtil.setItemPickupCooldown(pathfinderMob, SharedWolfAi.ITEM_PICKUP_COOLDOWN);
        AiUtil.eraseMemories(pathfinderMob, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER);

        pathfinderMob.setJumping(false);
        GenericAi.stopWalking(pathfinderMob);
        pathfinderMob.setTarget(null);
    }

    public static void commandAttack(PathfinderMob pet, LivingEntity target, LivingEntity owner) {
        handleStates(pet, false, true);
        yieldAsPet(pet);
        if(pet.canAttack(target)){
            StartAttacking.setAttackTarget(pet, target);
            pet.setTarget(target);
        }
    }

    public static void commandCome(PathfinderMob pet, LivingEntity owner, ServerLevel serverLevel) {
        handleStates(pet, false, true);
        yieldAsPet(pet);
        if(pet.closerThan(owner, FollowOwner.TELEPORT_DISTANCE)){
            navigateToTarget(pet, owner, SharedWolfAi.SPEED_MODIFIER_WALKING);
        } else{
            FollowOwner.teleportToEntity(owner, serverLevel, pet, pet instanceof FlyingAnimal);
        }
    }

    public static void commandFree(PathfinderMob pet, LivingEntity user) {
        commandFree(pet, user, true);
    }

    public static void commandFree(PathfinderMob pet, LivingEntity user, boolean handleDI) {
        if(CompatUtil.isDILoaded() && handleDI){
            DICompat.setDICommand(pet, user, DICompat.DI_WANDER_COMMAND);
        }
        handleStates(pet, false, true);
        yieldAsPet(pet);
        stopFollowing(pet);
        stopHeeling(pet);
    }

    public static void commandFollow(PathfinderMob pet, LivingEntity user) {
        commandFollow(pet, user, true);
    }

    public static void commandFollow(PathfinderMob pet, LivingEntity user, boolean handleDI) {
        if(CompatUtil.isDILoaded() && handleDI){
            DICompat.setDICommand(pet, user, DICompat.DI_FOLLOW_COMMAND);
        }
        handleStates(pet, false, true);
        yieldAsPet(pet);
        stopHeeling(pet);
        setFollowing(pet);
    }

    public static void commandGo(PathfinderMob pet, LivingEntity user, HitResult hitResult) {
        handleStates(pet, false, true);
        yieldAsPet(pet);
        stopFollowing(pet);
        stopHeeling(pet);
        if(hitResult instanceof BlockHitResult blockHitResult){
            navigateToTarget(pet, blockHitResult.getBlockPos(), SharedWolfAi.SPEED_MODIFIER_WALKING);
        } else if(hitResult instanceof EntityHitResult entityHitResult){
            navigateToTarget(pet, entityHitResult.getEntity(), SharedWolfAi.SPEED_MODIFIER_WALKING);
        }
    }

    public static void commandHeel(PathfinderMob pet, LivingEntity user) {
        if(CompatUtil.isDILoaded()){
            DICompat.setDICommand(pet, user, DICompat.DI_FOLLOW_COMMAND);
        }
        handleStates(pet, false, true);
        yieldAsPet(pet);
        stopFollowing(pet);
        setHeeling(pet);
    }

    public static void commandSit(PathfinderMob pet, LivingEntity user) {
        commandSit(pet, user, true);
    }

    public static void commandSit(PathfinderMob pet, LivingEntity user, boolean handleDI) {
        if(CompatUtil.isDILoaded() && handleDI){
            DICompat.setDICommand(pet, user, DICompat.DI_STAY_COMMAND);
        }
        handleStates(pet, true, false);
        pet.getBrain().setMemory(ABABMemoryModuleTypes.IS_ORDERED_TO_SIT.get(), Unit.INSTANCE);
        yieldAsPet(pet, ABABActivities.SIT.get());
    }

    @SuppressWarnings("SameParameterValue")
    private static void navigateToTarget(PathfinderMob mob, Object target, float speedModifier) {
        if(mob.getBrain().checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED)){
            if(target instanceof Entity entity){
                AiUtil.setWalkAndLookTargetMemories(mob, entity, speedModifier, 0);
            } else if(target instanceof BlockPos blockPos){
                AiUtil.setWalkAndLookTargetMemories(mob, blockPos, speedModifier, 0);
            }
        } else if(target instanceof Entity || target instanceof BlockPos){
            WalkTarget walkTarget;
            if (target instanceof Entity entity) {
                walkTarget = new WalkTarget(entity, speedModifier, 0);
            } else {
                BlockPos blockPos = (BlockPos) target;
                walkTarget = new WalkTarget(blockPos, speedModifier, 0);
            }
            PositionTracker lookTarget;
            if (target instanceof Entity entity) {
                lookTarget = new EntityTracker(entity, true);
            } else {
                BlockPos blockPos = (BlockPos) target;
                lookTarget = new BlockPosTracker(blockPos);
            }
            WalkTargetAccessor.cast(mob).setWalkTarget(walkTarget);
            LookTargetAccessor.cast(mob).setLookTarget(lookTarget);
        }
    }

    private static void handleStates(PathfinderMob pet, boolean orderedToSit, boolean resetSit) {
        if(pet instanceof TamableAnimal tamableAnimal){
            tamableAnimal.setOrderedToSit(orderedToSit);
            SharedWolfAi.clearStates(tamableAnimal, resetSit);
        } else{
            if(pet instanceof Fox fox){
                ReflectionUtil.callMethod(FOX_CLEAR_STATES, fox);
            }
        }
    }

    public static void setFollowing(LivingEntity entity) {
        entity.getBrain().setMemory(ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get(), Unit.INSTANCE);
    }

    public static void stopFollowing(LivingEntity entity) {
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
