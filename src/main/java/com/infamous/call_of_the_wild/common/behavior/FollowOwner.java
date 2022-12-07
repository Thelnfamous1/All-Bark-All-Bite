package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.util.AiHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

public class FollowOwner extends Behavior<TamableAnimal> {
    private static final int TELEPORT_WHEN_DISTANCE_IS = 12;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private LivingEntity owner;
    private final float speedModifier;
    private int timeToRecalcPath;
    private final int stopDistance;
    private final int startDistance;
    private float oldWaterCost;
    private final boolean canFly;

    public FollowOwner(float speedModifier, int startDistance, int stopDistance) {
        this(speedModifier, startDistance, stopDistance, false);
    }

    public FollowOwner(float speedModifier, int startDistance, int stopDistance, boolean canFly) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.canFly = canFly;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, TamableAnimal tamable) {
        LivingEntity owner = tamable.getOwner();
        if (owner == null) {
            return false;
        } else if (owner.isSpectator()) {
            return false;
        } else if (tamable.isOrderedToSit()) {
            return false;
        } else if (tamable.distanceToSqr(owner) < (double)(this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = owner;
            return true;
        }
    }

    @Override
    public boolean canStillUse(ServerLevel level, TamableAnimal tamable, long gameTime) {
        if (tamable.getNavigation().isDone()) {
            return false;
        } else if (tamable.isOrderedToSit()) {
            return false;
        } else {
            return !(tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
        }
    }

    @Override
    public void start(ServerLevel level, TamableAnimal tamable, long gameTime) {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = tamable.getPathfindingMalus(BlockPathTypes.WATER);
        tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        BehaviorUtils.setWalkAndLookTargetMemories(tamable, this.owner, this.speedModifier, this.startDistance);
    }

    @Override
    public void stop(ServerLevel level, TamableAnimal tamable, long gameTime) {
        this.owner = null;
        AiHelper.stopWalking(tamable);
        tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick(ServerLevel level, TamableAnimal tamable, long gameTime) {
        BehaviorUtils.lookAtEntity(tamable, this.owner);
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = AiHelper.reducedTickDelay(10);
            if (!tamable.isLeashed() && !tamable.isPassenger()) {
                if (tamable.distanceToSqr(this.owner) >= TELEPORT_WHEN_DISTANCE_IS * TELEPORT_WHEN_DISTANCE_IS) {
                    this.teleportToOwner(level, tamable);
                }
            }
        }
    }

    private void teleportToOwner(ServerLevel level, TamableAnimal tamable) {
        BlockPos blockpos = this.owner.blockPosition();

        for(int i = 0; i < 10; ++i) {
            int xOffset = this.randomIntInclusive(tamable,
                    -MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            int yOffset = this.randomIntInclusive(tamable,
                    -MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            int zOffset = this.randomIntInclusive(tamable,
                    -MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            boolean teleported = this.maybeTeleportTo(level, tamable, blockpos.getX() + xOffset, blockpos.getY() + yOffset, blockpos.getZ() + zOffset);
            if (teleported) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(ServerLevel level, TamableAnimal tamable, int x, int y, int z) {
        if (Math.abs((double)x - this.owner.getX()) < MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING
                && Math.abs((double)z - this.owner.getZ()) < MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING) {
            return false;
        } else if (!this.canTeleportTo(level, tamable, new BlockPos(x, y, z))) {
            return false;
        } else {
            tamable.moveTo((double)x + 0.5D, (double)y, (double)z + 0.5D, tamable.getYRot(), tamable.getXRot());
            AiHelper.stopWalking(tamable);
            return true;
        }
    }

    private boolean canTeleportTo(ServerLevel level, TamableAnimal tamable, BlockPos targetPos) {
        BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(level, targetPos.mutable());
        if (blockpathtypes != BlockPathTypes.WALKABLE) {
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

    private int randomIntInclusive(TamableAnimal tamable, int min, int max) {
        return tamable.getRandom().nextInt(max - min + 1) + min;
    }
}
