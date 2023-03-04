package com.infamous.all_bark_all_bite.common.behavior.pet;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.compat.CompatUtil;
import com.infamous.all_bark_all_bite.common.compat.DICompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class FollowOwner<E extends PathfinderMob> extends Behavior<E> {
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final Predicate<E> dontFollowIf;

    private final Function<E, Optional<LivingEntity>> entityGetter;
    private final float speedModifier;
    private final int closeEnough;
    private final ToIntFunction<E> tooFarFunction;
    private int calculatePathCounter;
    private float oldWaterCost;
    private final boolean canFly;

    public FollowOwner(Function<E, Optional<LivingEntity>> entityGetter, float speedModifier, int closeEnough, int tooFar){
        this(mob -> false, entityGetter, speedModifier, closeEnough, value -> tooFar);
    }

    public FollowOwner(Predicate<E> dontFollowIf, Function<E, Optional<LivingEntity>> entityGetter, float speedModifier, int closeEnough, ToIntFunction<E> tooFarFunction) {
        this(dontFollowIf, entityGetter, speedModifier, closeEnough, tooFarFunction, false);
    }

    public FollowOwner(Predicate<E> dontFollowIf, Function<E, Optional<LivingEntity>> entityGetter, float speedModifier, int closeEnough, ToIntFunction<E> tooFarFunction, boolean canFly) {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.IS_FOLLOWING.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
        this.dontFollowIf = dontFollowIf;
        this.entityGetter = entityGetter;
        this.speedModifier = speedModifier;
        this.closeEnough = closeEnough;
        this.tooFarFunction = tooFarFunction;
        this.canFly = canFly;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (this.dontFollowIf(mob)) {
            return false;
        } else{
            Optional<LivingEntity> optional = this.getOwner(mob);
            if (optional.isEmpty() || optional.get().isSpectator()) {
                return false;
            } else {
                LivingEntity target = optional.get();
                return !mob.closerThan(target, this.tooFarFunction.applyAsInt(mob));
            }
        }
    }

    private Optional<LivingEntity> getOwner(E mob) {
        return this.entityGetter.apply(mob);
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        mob.getBrain().setMemory(ABABMemoryModuleTypes.IS_FOLLOWING.get(), Unit.INSTANCE);
        this.calculatePathCounter = 0;
        this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
        mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.goToEntity(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected void goToEntity(E mob) {
        AiUtil.setWalkAndLookTargetMemories(mob, this.getOwner(mob).get(), this.speedModifier, this.closeEnough);
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    public boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        if (this.dontFollowIf(mob)) {
            return false;
        } else {
            Optional<LivingEntity> entityOptional = this.getOwner(mob);
            if (entityOptional.isEmpty()) {
                return false;
            } else {
                LivingEntity entity = entityOptional.get();
                return mob.distanceToSqr(entity) > Mth.square(this.closeEnough);
            }
        }
    }

    private boolean dontFollowIf(E mob) {
        return this.dontFollowIf.test(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void tick(ServerLevel level, E mob, long gameTime) {
        LivingEntity owner = this.getOwner(mob).get();
        BehaviorUtils.lookAtEntity(mob, owner);

        if (--this.calculatePathCounter <= 0) {
            this.calculatePathCounter = 10;
            if (!mob.isLeashed() && !mob.isPassenger()) {
                if (!mob.closerThan(owner, ABABConfig.petTeleportDistanceTrigger.get())) {
                    FollowOwner.teleportToEntity(owner, level, mob, this.canFly);
                } else {
                    this.goToEntity(mob);
                }
            }
        }
    }

    public static void teleportToEntity(LivingEntity owner, ServerLevel level, PathfinderMob mob, boolean canFly) {
        BlockPos ownerCurrentBlockPos = owner.blockPosition();
        for(int i = 0; i < 10; ++i) {
            int xOffset = FollowOwner.randomIntInclusive(mob,
                    -MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            int yOffset = FollowOwner.randomIntInclusive(mob,
                    -MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            int zOffset = FollowOwner.randomIntInclusive(mob,
                    -MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            boolean teleported = FollowOwner.maybeTeleportTo(owner, level, mob, ownerCurrentBlockPos.getX() + xOffset, ownerCurrentBlockPos.getY() + yOffset, ownerCurrentBlockPos.getZ() + zOffset, canFly);
            if (teleported) {
                return;
            }
        }

    }

    private static boolean maybeTeleportTo(LivingEntity owner, ServerLevel level, PathfinderMob mob, int x, int y, int z, boolean canFly) {
        Vec3 ownerCurrentPos = owner.position();
        if (Math.abs((double)x - ownerCurrentPos.x) < MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING
                && Math.abs((double)z - ownerCurrentPos.z) < MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING) {
            return false;
        } else if (!FollowOwner.canTeleportTo(level, mob, new BlockPos(x, y, z), canFly)) {
            return false;
        } else {
            mob.moveTo((double)x + 0.5D, y, (double)z + 0.5D, mob.getYRot(), mob.getXRot());
            GenericAi.stopWalking(mob);
            return true;
        }
    }

    private static boolean canTeleportTo(ServerLevel level, PathfinderMob mob, BlockPos targetPos, boolean canFly) {
        if(CompatUtil.isDILoaded()){
            if(DICompat.hasDIAmphibiousEnchant(mob) && level.isWaterAt(targetPos)){
                return true;
            }
        }
        BlockPathTypes pathTypes = WalkNodeEvaluator.getBlockPathTypeStatic(level, targetPos.mutable());
        if (pathTypes != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = level.getBlockState(targetPos.below());
            if (!canFly && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = targetPos.subtract(mob.blockPosition());
                return level.noCollision(mob, mob.getBoundingBox().move(blockpos));
            }
        }
    }

    private static int randomIntInclusive(PathfinderMob mob, int min, int max) {
        return mob.getRandom().nextInt(max - min + 1) + min;
    }

    @Override
    public void stop(ServerLevel level, E mob, long gameTime) {
        mob.getBrain().eraseMemory(ABABMemoryModuleTypes.IS_FOLLOWING.get());
        GenericAi.stopWalking(mob);
        mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }
}
