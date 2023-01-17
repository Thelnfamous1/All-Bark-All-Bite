package com.infamous.call_of_the_wild.common.behavior.pet;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
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

@SuppressWarnings("NullableProblems")
public class FollowOwner<E extends TamableAnimal> extends Behavior<E> {
    private static final int TELEPORT_WHEN_DISTANCE_IS = 12;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final float speedModifier;
    private final int closeEnough;
    private final int tooFar;
    private int calculatePathCounter;
    private float oldWaterCost;
    private final boolean canFly;

    public FollowOwner(float speedModifier, int closeEnough, int tooFar) {
        this(speedModifier, closeEnough, tooFar, false);
    }

    public FollowOwner(float speedModifier, int closeEnough, int tooFar, boolean canFly) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
        this.speedModifier = speedModifier;
        this.closeEnough = closeEnough;
        this.tooFar = tooFar;
        this.canFly = canFly;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, E tamable) {
        if (tamable.isOrderedToSit()) {
            return false;
        } else{
            Optional<LivingEntity> optional = this.getOwner(tamable);
            if (optional.isEmpty() || optional.get().isSpectator()) {
                return false;
            } else {
                LivingEntity target = optional.get();
                return !tamable.closerThan(target, this.tooFar);
            }
        }
    }

    private Optional<LivingEntity> getOwner(E tamable) {
        return AiUtil.getOwner(tamable);
    }

    @Override
    public void start(ServerLevel level, E tamable, long gameTime) {
        this.calculatePathCounter = 0;
        this.oldWaterCost = tamable.getPathfindingMalus(BlockPathTypes.WATER);
        tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.goToOwner(tamable);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected void goToOwner(E tamable) {
        AiUtil.setWalkAndLookTargetMemories(tamable, this.getOwner(tamable).get(), this.speedModifier, this.closeEnough);
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    public boolean canStillUse(ServerLevel level, E tamable, long gameTime) {
        if (tamable.getNavigation().isDone()) {
            return false;
        } else if (tamable.isOrderedToSit()) {
            return false;
        } else {
            Optional<LivingEntity> optional = this.getOwner(tamable);
            if (optional.isEmpty()) {
                return false;
            } else {
                LivingEntity target = optional.get();
                return tamable.distanceToSqr(target) > Mth.square(this.closeEnough);
            }
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void tick(ServerLevel level, E tamable, long gameTime) {
        LivingEntity target = this.getOwner(tamable).get();
        BehaviorUtils.lookAtEntity(tamable, target);

        if (--this.calculatePathCounter <= 0) {
            this.calculatePathCounter = 10;
            if (!tamable.isLeashed() && !tamable.isPassenger()) {
                if (!tamable.position().closerThan(target.position(), TELEPORT_WHEN_DISTANCE_IS)) {
                    this.teleportToOwner(target, level, tamable);
                } else {
                    this.goToOwner(tamable);
                }
            }
        }
    }

    private void teleportToOwner(LivingEntity ownerPositionTracker, ServerLevel level, E tamable) {
        BlockPos ownerCurrentBlockPos = ownerPositionTracker.blockPosition();
        for(int i = 0; i < 10; ++i) {
            int xOffset = this.randomIntInclusive(tamable,
                    -MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            int yOffset = this.randomIntInclusive(tamable,
                    -MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            int zOffset = this.randomIntInclusive(tamable,
                    -MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            boolean teleported = this.maybeTeleportTo(ownerPositionTracker, level, tamable, ownerCurrentBlockPos.getX() + xOffset, ownerCurrentBlockPos.getY() + yOffset, ownerCurrentBlockPos.getZ() + zOffset);
            if (teleported) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(LivingEntity target, ServerLevel level, E tamable, int x, int y, int z) {
        Vec3 ownerCurrentPos = target.position();
        if (Math.abs((double)x - ownerCurrentPos.x) < MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING
                && Math.abs((double)z - ownerCurrentPos.z) < MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING) {
            return false;
        } else if (!this.canTeleportTo(level, tamable, new BlockPos(x, y, z))) {
            return false;
        } else {
            tamable.moveTo((double)x + 0.5D, y, (double)z + 0.5D, tamable.getYRot(), tamable.getXRot());
            GenericAi.stopWalking(tamable);
            return true;
        }
    }

    private boolean canTeleportTo(ServerLevel level, E tamable, BlockPos targetPos) {
        BlockPathTypes pathTypes = WalkNodeEvaluator.getBlockPathTypeStatic(level, targetPos.mutable());
        if (pathTypes != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = level.getBlockState(targetPos.below());
            if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = targetPos.subtract(tamable.blockPosition());
                return level.noCollision(tamable, tamable.getBoundingBox().move(blockpos));
            }
        }
    }

    private int randomIntInclusive(E tamable, int min, int max) {
        return tamable.getRandom().nextInt(max - min + 1) + min;
    }
    public void stop(ServerLevel level, E tamableAnimal, long gameTime) {
        GenericAi.stopWalking(tamableAnimal);
        tamableAnimal.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }
}
