package com.infamous.all_bark_all_bite.common.goal;

import com.infamous.all_bark_all_bite.common.entity.WalkTargetAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class MoveToTargetSinkGoal extends Goal {
    private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;
    private final PathfinderMob mob;
    private final WalkTargetAccessor walkTargetAccessor;
    private long lastAttemptTimestamp;
    @Nullable
    private Path path;
    @Nullable
    private BlockPos lastTargetPos;
    private float speedModifier;
    private long endTimestamp;

    public MoveToTargetSinkGoal(PathfinderMob mob) {
        this.mob = mob;
        this.walkTargetAccessor = WalkTargetAccessor.cast(this.mob);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        WalkTarget walkTarget = this.walkTargetAccessor.getWalkTarget();
        if(this.mob.isPathFinding() || walkTarget == null){
            return false;
        } else if (this.lastAttemptTimestamp > this.mob.level.getGameTime()) {
            return false;
        } else {
            boolean flag = this.reachedTarget(this.mob, walkTarget);
            if (!flag && this.tryComputePath(this.mob, walkTarget)) {
                this.lastTargetPos = walkTarget.getTarget().currentBlockPosition();
                return true;
            } else {
                this.walkTargetAccessor.setWalkTarget(null);
                return false;
            }
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        int duration = 150 + this.mob.getRandom().nextInt(250 + 1 - 150);
        this.endTimestamp = this.mob.level.getGameTime() + (long)duration;
    }

    @Override
    public boolean canContinueToUse() {
        if(this.mob.level.getGameTime() > this.endTimestamp){
            return false;
        } else if (this.path != null && this.lastTargetPos != null) {
            WalkTarget walkTarget = this.walkTargetAccessor.getWalkTarget();
            PathNavigation pathNavigation = this.mob.getNavigation();
            return !pathNavigation.isDone() && walkTarget != null && !this.reachedTarget(this.mob, walkTarget);
        } else {
            return false;
        }
    }

    @Override
    public void tick() {
        Path path = this.mob.getNavigation().getPath();
        if (this.path != path) {
            this.path = path;
        }

        if (path != null && this.lastTargetPos != null) {
            WalkTarget walktarget = this.walkTargetAccessor.getWalkTarget();
            if (walktarget != null && walktarget.getTarget().currentBlockPosition().distSqr(this.lastTargetPos) > 4.0D && this.tryComputePath(this.mob, walktarget)) {
                this.lastTargetPos = walktarget.getTarget().currentBlockPosition();
                this.start();
            }

        }
    }

    @Override
    public void stop() {
        WalkTarget walkTarget = this.walkTargetAccessor.getWalkTarget();
        if (walkTarget != null && !this.reachedTarget(this.mob, walkTarget) && this.mob.getNavigation().isStuck()) {
            this.lastAttemptTimestamp = this.mob.level.getGameTime() + this.mob.getRandom().nextInt(MAX_COOLDOWN_BEFORE_RETRYING);
        }

        this.mob.getNavigation().stop();
        this.walkTargetAccessor.setWalkTarget(null);
        this.path = null;
    }

    private boolean tryComputePath(PathfinderMob mob, WalkTarget walkTarget) {
        BlockPos targetBlockPos = walkTarget.getTarget().currentBlockPosition();
        this.path = mob.getNavigation().createPath(targetBlockPos, 0);
        this.speedModifier = walkTarget.getSpeedModifier();
        if (this.path != null) {
            return true;
        }
        Vec3 posTowards = DefaultRandomPos.getPosTowards(mob, 10, 7, Vec3.atBottomCenterOf(targetBlockPos), (float)Math.PI / 2F);
        if (posTowards != null) {
            this.path = mob.getNavigation().createPath(posTowards.x, posTowards.y, posTowards.z, 0);
            return this.path != null;
        }
        return false;
    }

    private boolean reachedTarget(Mob mob, WalkTarget walkTarget) {
        return walkTarget.getTarget().currentBlockPosition().distManhattan(mob.blockPosition()) <= walkTarget.getCloseEnoughDist();
    }
}
