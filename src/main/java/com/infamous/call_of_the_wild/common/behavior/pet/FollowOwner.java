package com.infamous.call_of_the_wild.common.behavior.pet;

import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.StayCloseToTarget;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("NullableProblems")
public class FollowOwner extends StayCloseToTarget<TamableAnimal> {
    private static final int TELEPORT_WHEN_DISTANCE_IS = 12;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final Function<LivingEntity, Optional<PositionTracker>> ownerPositionGetter;
    private int timeToRecalcPath;
    private float oldWaterCost;
    private final boolean canFly;

    public FollowOwner(Function<LivingEntity, Optional<PositionTracker>> ownerPositionGetter, float speedModifier, int closeEnough, int tooFar) {
        this(ownerPositionGetter, speedModifier, closeEnough, tooFar, false);
    }

    public FollowOwner(Function<LivingEntity, Optional<PositionTracker>> ownerPositionGetter, float speedModifier, int closeEnough, int tooFar, boolean canFly) {
        super(ownerPositionGetter, closeEnough, tooFar, speedModifier);
        this.ownerPositionGetter = ownerPositionGetter;
        this.canFly = canFly;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, TamableAnimal tamable) {
        if (tamable.isOrderedToSit()) {
            return false;
        } else{
            return super.checkExtraStartConditions(level, tamable);
        }
    }

    @Override
    public void start(ServerLevel level, TamableAnimal tamable, long gameTime) {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = tamable.getPathfindingMalus(BlockPathTypes.WATER);
        tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        super.start(level, tamable, gameTime);
    }

    public boolean canStillUse(ServerLevel level, TamableAnimal tamable, long gameTime) {
        if (tamable.getNavigation().isDone()) {
            return false;
        } else if (tamable.isOrderedToSit()) {
            return false;
        } else {
            return super.checkExtraStartConditions(level, tamable);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void tick(ServerLevel level, TamableAnimal tamable, long gameTime) {
        PositionTracker ownerPositionTracker = this.ownerPositionGetter.apply(tamable).get();
        tamable.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, ownerPositionTracker);

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = AiUtil.reducedTickDelay(10);
            if (!tamable.isLeashed() && !tamable.isPassenger()) {
                if (!tamable.position().closerThan(ownerPositionTracker.currentPosition(), TELEPORT_WHEN_DISTANCE_IS)) {
                    this.teleportToOwner(ownerPositionTracker, level, tamable);
                } else {
                    super.start(level, tamable, gameTime);
                }
            }
        }
    }

    private void teleportToOwner(PositionTracker ownerPositionTracker, ServerLevel level, TamableAnimal tamable) {
        BlockPos ownerCurrentBlockPos = ownerPositionTracker.currentBlockPosition();
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

    private boolean maybeTeleportTo(PositionTracker ownerPositionTracker, ServerLevel level, TamableAnimal tamable, int x, int y, int z) {
        Vec3 ownerCurrentPos = ownerPositionTracker.currentPosition();
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

    private boolean canTeleportTo(ServerLevel level, TamableAnimal tamable, BlockPos targetPos) {
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

    private int randomIntInclusive(TamableAnimal tamable, int min, int max) {
        return tamable.getRandom().nextInt(max - min + 1) + min;
    }
    public void stop(ServerLevel level, TamableAnimal tamableAnimal, long gameTime) {
        GenericAi.stopWalking(tamableAnimal);
        tamableAnimal.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }
}
