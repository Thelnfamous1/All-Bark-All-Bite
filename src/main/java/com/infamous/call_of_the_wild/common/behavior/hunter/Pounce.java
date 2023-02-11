package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import com.infamous.call_of_the_wild.common.ai.HunterAi;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Pounce extends Behavior<PathfinderMob> {

    private static final double MIN_Y_DELTA = 0.05D;
    private final int pounceHeight;
    private final int pounceDistance;

    public Pounce(int pounceHeight, int pounceDistance) {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.POUNCE_TARGET.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.pounceDistance = pounceDistance;
        this.pounceHeight = pounceHeight;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
        if (!mob.hasPose(Pose.CROUCHING)) {
            HunterAi.stopPouncing(mob);
            return false;
        } else {
            LivingEntity pounceTarget = this.getPounceTarget(mob).orElse(null);
            if (pounceTarget != null && pounceTarget.isAlive()) {
                if (pounceTarget.getMotionDirection() != pounceTarget.getDirection()) {
                    HunterAi.stopPouncing(mob);
                    return false;
                } else {
                    boolean pathClear = AiUtil.isPathClear(mob, pounceTarget, this.pounceDistance, this.pounceHeight);
                    if (!pathClear) {
                        //mob.getNavigation().createPath(pounceTarget, 0);
                        HunterAi.stopPouncing(mob);
                        if(mob.hasPose(Pose.CROUCHING)) mob.setPose(Pose.STANDING);
                    }

                    return pathClear;
                }
            } else {
                HunterAi.stopPouncing(mob);
                return false;
            }
        }
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        //mob.setJumping(true);
        mob.setPose(Pose.LONG_JUMPING);
        LivingEntity pounceTarget = this.getPounceTarget(mob).orElse(null);
        if (pounceTarget != null) {
            BehaviorUtils.lookAtEntity(mob, pounceTarget);
            Vec3 jumpVector = pounceTarget.position().subtract(mob.position()).normalize();
            double xzDScale = this.pounceDistance * 2.0D / 15.0D; // 0.8D for 6
            double yD = this.pounceHeight * 3.0D / 10.0D; // 0.9D for 3
            mob.setDeltaMovement(mob.getDeltaMovement().add(jumpVector.x * xzDScale, yD, jumpVector.z * xzDScale));
        }

        GenericAi.stopWalking(mob);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob mob, long gameTime) {
        LivingEntity pounceTarget = this.getPounceTarget(mob).orElse(null);
        if (pounceTarget != null && pounceTarget.isAlive()) {
            double yDelta = mob.getDeltaMovement().y;
            return (!(Mth.square(yDelta) < MIN_Y_DELTA) || !mob.isOnGround());
        } else {
            return false;
        }
    }

    @NotNull
    private Optional<LivingEntity> getPounceTarget(PathfinderMob mob) {
        return HunterAi.getPounceTarget(mob);
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob mob, long gameTime) {
        LivingEntity pounceTarget = this.getPounceTarget(mob).orElse(null);
        if (pounceTarget != null) {
            BehaviorUtils.lookAtEntity(mob, pounceTarget);
        }

        /*
        if (pounceTarget != null && mob.distanceTo(pounceTarget) <= 2.0F) {
            mob.doHurtTarget(pounceTarget);
        }
         */
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob mob, long gameTime) {
        if(mob.hasPose(Pose.LONG_JUMPING)){
            mob.setPose(Pose.STANDING);
        }
        HunterAi.stopPouncing(mob);
    }
}
