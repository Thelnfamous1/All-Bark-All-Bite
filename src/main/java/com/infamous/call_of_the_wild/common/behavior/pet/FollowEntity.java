package com.infamous.call_of_the_wild.common.behavior.pet;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
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

@SuppressWarnings("NullableProblems")
public class FollowEntity<E extends PathfinderMob> extends Behavior<E> {
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final Predicate<E> dontFollowIf;

    private final Function<E, Optional<LivingEntity>> entityGetter;
    private final float speedModifier;
    private final UniformInt followRange;
    private final int teleportBuffer;
    private int calculatePathCounter;
    private float oldWaterCost;
    private final boolean canFly;

    public FollowEntity(Predicate<E> dontFollowIf, Function<E, Optional<LivingEntity>> entityGetter, float speedModifier, UniformInt followRange, int teleportBuffer) {
        this(dontFollowIf, entityGetter, speedModifier, followRange, teleportBuffer, false);
    }

    public FollowEntity(Predicate<E> dontFollowIf, Function<E, Optional<LivingEntity>> entityGetter, float speedModifier, UniformInt followRange, int teleportBuffer, boolean canFly) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
        this.dontFollowIf = dontFollowIf;
        this.entityGetter = entityGetter;
        this.speedModifier = speedModifier;
        this.followRange = followRange;
        this.teleportBuffer = teleportBuffer;
        this.canFly = canFly;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (this.dontFollowIf(mob)) {
            return false;
        } else{
            Optional<LivingEntity> optional = this.getEntity(mob);
            if (optional.isEmpty() || optional.get().isSpectator()) {
                return false;
            } else {
                LivingEntity target = optional.get();
                return !mob.closerThan(target, this.followRange.getMaxValue());
            }
        }
    }

    private Optional<LivingEntity> getEntity(E mob) {
        return this.entityGetter.apply(mob);
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        this.calculatePathCounter = 0;
        this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
        mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.goToEntity(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected void goToEntity(E mob) {
        AiUtil.setWalkAndLookTargetMemories(mob, this.getEntity(mob).get(), this.speedModifier, this.followRange.getMinValue());
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    public boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        if (mob.getNavigation().isDone()) {
            return false;
        } else if (this.dontFollowIf(mob)) {
            return false;
        } else {
            Optional<LivingEntity> entityOptional = this.getEntity(mob);
            if (entityOptional.isEmpty()) {
                return false;
            } else {
                LivingEntity entity = entityOptional.get();
                return mob.distanceToSqr(entity) > Mth.square(this.followRange.getMinValue());
            }
        }
    }

    private boolean dontFollowIf(E mob) {
        return this.dontFollowIf.test(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void tick(ServerLevel level, E mob, long gameTime) {
        LivingEntity entity = this.getEntity(mob).get();
        BehaviorUtils.lookAtEntity(mob, entity);

        if (--this.calculatePathCounter <= 0) {
            this.calculatePathCounter = 10;
            if (!mob.isLeashed() && !mob.isPassenger()) {
                if (!mob.position().closerThan(entity.position(), this.teleportDistance())) {
                    this.teleportToEntity(entity, level, mob);
                } else {
                    this.goToEntity(mob);
                }
            }
        }
    }

    private int teleportDistance(){
        return this.followRange.getMaxValue() + this.teleportBuffer;
    }

    private void teleportToEntity(LivingEntity owner, ServerLevel level, E mob) {
        BlockPos ownerCurrentBlockPos = owner.blockPosition();
        for(int i = 0; i < 10; ++i) {
            int xOffset = this.randomIntInclusive(mob,
                    -MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            int yOffset = this.randomIntInclusive(mob,
                    -MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            int zOffset = this.randomIntInclusive(mob,
                    -MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING, MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING);
            boolean teleported = this.maybeTeleportTo(owner, level, mob, ownerCurrentBlockPos.getX() + xOffset, ownerCurrentBlockPos.getY() + yOffset, ownerCurrentBlockPos.getZ() + zOffset);
            if (teleported) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(LivingEntity entity, ServerLevel level, E mob, int x, int y, int z) {
        Vec3 ownerCurrentPos = entity.position();
        if (Math.abs((double)x - ownerCurrentPos.x) < MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING
                && Math.abs((double)z - ownerCurrentPos.z) < MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING) {
            return false;
        } else if (!this.canTeleportTo(level, mob, new BlockPos(x, y, z))) {
            return false;
        } else {
            mob.moveTo((double)x + 0.5D, y, (double)z + 0.5D, mob.getYRot(), mob.getXRot());
            GenericAi.stopWalking(mob);
            return true;
        }
    }

    private boolean canTeleportTo(ServerLevel level, E mob, BlockPos targetPos) {
        BlockPathTypes pathTypes = WalkNodeEvaluator.getBlockPathTypeStatic(level, targetPos.mutable());
        if (pathTypes != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = level.getBlockState(targetPos.below());
            if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = targetPos.subtract(mob.blockPosition());
                return level.noCollision(mob, mob.getBoundingBox().move(blockpos));
            }
        }
    }

    private int randomIntInclusive(E mob, int min, int max) {
        return mob.getRandom().nextInt(max - min + 1) + min;
    }
    public void stop(ServerLevel level, E mob, long gameTime) {
        GenericAi.stopWalking(mob);
        mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }
}
