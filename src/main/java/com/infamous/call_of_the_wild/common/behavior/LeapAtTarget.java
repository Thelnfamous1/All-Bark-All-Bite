package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class LeapAtTarget extends Behavior<PathfinderMob> {
    private static final int CROUCH_ANIMATION_DURATION = 4;
    private final float yD;
    private final int tooClose;
    private final int tooFar;
    private int crouchAnimationTimer;
    private LeapAtTarget.State state = LeapAtTarget.State.DONE;
    private final float chance;

    public LeapAtTarget(float chance, float yD, int tooClose, int tooFar) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 100);
        this.chance = chance;
        this.yD = yD;
        this.tooClose = tooClose;
        this.tooFar = tooFar;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
        if (mob.isVehicle()) {
            return false;
        } else {
            LivingEntity attackTarget = GenericAi.getAttackTarget(mob).get();
            if (!mob.closerThan(attackTarget, this.tooClose) && mob.distanceToSqr(attackTarget) <= Mth.square(this.tooFar)) {
                if (!mob.isOnGround()) {
                    return false;
                } else {
                    return mob.getRandom().nextFloat() < this.chance;
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        this.state = State.CROUCH_ANIMATION;
        mob.setPose(Pose.CROUCHING);
        this.crouchAnimationTimer = 0;
        GenericAi.stopWalking(mob);
    }

    @Override
    public boolean canStillUse(ServerLevel level, PathfinderMob mob, long gameTime) {
        LivingEntity attackTarget = GenericAi.getAttackTarget(mob).orElse(null);
        if(attackTarget != null){
            switch (this.state){
                case CROUCH_ANIMATION, LEAP -> {
                    return attackTarget.isAlive() && mob.hasPose(Pose.CROUCHING);
                }
                case MID_LEAP -> {
                    return !mob.isOnGround();
                }
                default -> {
                    return false;
                }
            }
        } else{
            return false;
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void tick(ServerLevel p_22551_, PathfinderMob mob, long p_22553_) {
        LivingEntity target = GenericAi.getAttackTarget(mob).get();
        switch (this.state){
            case CROUCH_ANIMATION -> {
                if (this.crouchAnimationTimer++ >= CROUCH_ANIMATION_DURATION) {
                    this.state = LeapAtTarget.State.LEAP;
                }
            }
            case LEAP -> {
                mob.setPose(Pose.LONG_JUMPING);
                Vec3 deltaMovement = mob.getDeltaMovement();
                Vec3 xzD = new Vec3(target.getX() - mob.getX(), 0.0D, target.getZ() - mob.getZ());
                if (xzD.lengthSqr() > AiUtil.NEAR_ZERO_DELTA_MOVEMENT) {
                    xzD = xzD.normalize().scale(0.4D).add(deltaMovement.scale(0.2D));
                }

                mob.setDeltaMovement(xzD.x, this.yD, xzD.z);
                this.state = LeapAtTarget.State.MID_LEAP;
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob mob, long gameTime) {
        this.state = LeapAtTarget.State.DONE;
        if(mob.hasPose(Pose.CROUCHING) || mob.hasPose(Pose.LONG_JUMPING)){
            mob.setPose(Pose.STANDING);
        }
    }

    enum State {
        CROUCH_ANIMATION,
        LEAP,
        MID_LEAP,
        DONE
    }

}
