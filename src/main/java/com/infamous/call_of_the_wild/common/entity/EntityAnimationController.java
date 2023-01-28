package com.infamous.call_of_the_wild.common.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class EntityAnimationController<T extends LivingEntity> {
    protected static final double NEAR_ZERO_DELTA_MOVEMENT = 1.0E-6D;
    public static final byte JUMPING_EVENT_ID = (byte) 1;
    public static final byte ATTACKING_EVENT_ID = (byte) 4;

    protected final T entity;
    protected final EntityDataAccessor<Pose> entityPose;
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState babyAnimationState = new AnimationState();

    public final AnimationState crouchAnimationState = new AnimationState();

    public final AnimationState diggingAnimationState = new AnimationState();
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState idleSleepAnimationState = new AnimationState();
    public final AnimationState jumpAnimationState = new AnimationState();

    public final AnimationState leapAnimationState = new AnimationState();

    public final AnimationState sleepAnimationState = new AnimationState();
    public final AnimationState sprintAnimationState = new AnimationState();
    public final AnimationState walkAnimationState = new AnimationState();
    private int jumpTicks;
    private int jumpDuration;
    private int attackAnimationRemainingTicks;
    private int idleSleepDelayTicks;
    private boolean posed;

    public EntityAnimationController(T entity, EntityDataAccessor<Pose> entityPose) {
        this.entity = entity;
        this.entityPose = entityPose;
    }

    public void startAttackAnimation(){
        this.attackAnimationState.start(this.entity.tickCount);
        this.attackAnimationRemainingTicks = 15; //0.75 seconds, same length as attack animation
    }

    public void startJumpAnimation() {
        this.jumpAnimationState.start(this.entity.tickCount);
        this.jumpDuration = 10; // 0.5 seconds, same length as jump animation
        this.jumpTicks = 0;
    }

    public void onSyncedDataUpdatedAnimations(EntityDataAccessor<?> dataAccessor){
        if (this.entityPose.equals(dataAccessor)) {
            this.posed = false;
            this.handlePose(Pose.CROUCHING, this.crouchAnimationState);
            this.handlePose(Pose.DIGGING, this.diggingAnimationState);
            this.handlePose(Pose.LONG_JUMPING, this.leapAnimationState);
            this.handleSleepingPose();
            if(this.posed) this.stopAllNonePoseAnimations();
        }
    }

    private void handleSleepingPose() {
        if(this.entity.getPose() == Pose.SLEEPING) {
            if (!this.sleepAnimationState.isStarted()) {
                this.idleSleepDelayTicks = 4; // 0.2 seconds, same length as sit animation
            }
            this.sleepAnimationState.startIfStopped(this.entity.tickCount);
            this.posed = true;
        } else{
            this.sleepAnimationState.stop();
            this.idleSleepAnimationState.stop();
        }
    }

    private void handlePose(Pose pose, AnimationState poseAnimationState) {
        if(this.entity.getPose() == pose) {
            poseAnimationState.startIfStopped(this.entity.tickCount);
            this.posed = true;
        } else{
            poseAnimationState.stop();
        }
    }

    protected void stopAllNonePoseAnimations() {
        this.attackAnimationState.stop();
        this.idleAnimationState.stop();
        this.jumpAnimationState.stop();
        this.sprintAnimationState.stop();
        this.walkAnimationState.stop();
    }

    public void aiStepAnimations() {
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
        }

        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
        }
        if (this.idleSleepDelayTicks > 0) {
            --this.idleSleepDelayTicks;
        }
    }

    public void tickAnimations() {
        this.tickBaby();
        this.tickBasicAnimations();
    }

    protected void tickBasicAnimations() {
        if(!this.entity.isSleeping()){
            if(!this.posed){
                this.tickNonPoseAnimations();
            }
        } else{
            if (this.idleSleepDelayTicks == 0) {
                this.sleepAnimationState.stop();
                this.idleSleepAnimationState.startIfStopped(this.entity.tickCount);
            }
        }
    }

    protected void tickBaby() {
        if (this.entity.isBaby()) {
            this.babyAnimationState.startIfStopped(this.entity.tickCount);
        } else {
            this.babyAnimationState.stop();
        }
    }

    private void tickNonPoseAnimations() {
        boolean midJump = this.jumpDuration > 0;
        if (!midJump && this.jumpAnimationState.isStarted()) this.jumpAnimationState.stop();

        boolean midAttack = this.attackAnimationRemainingTicks > 0;
        if(!midAttack && this.attackAnimationState.isStarted()) this.attackAnimationState.stop();

        boolean inactive = !midJump && !midAttack;
        if (this.isMovingOnLandOrInWater() && inactive) {
            this.idleAnimationState.stop();
            if (this.entity.isSprinting()) {
                this.walkAnimationState.stop();
                this.sprintAnimationState.startIfStopped(this.entity.tickCount);
            } else {
                this.sprintAnimationState.stop();
                this.walkAnimationState.startIfStopped(this.entity.tickCount);
            }
        } else {
            if (inactive) this.idleAnimationState.startIfStopped(this.entity.tickCount);
            this.sprintAnimationState.stop();
            this.walkAnimationState.stop();
        }
    }

    private boolean isMovingOnLand() {
        return this.entity.isOnGround() && this.isMoving() && !this.entity.isInWaterOrBubble();
    }

    private boolean isMoving() {
        return this.entity.getDeltaMovement().horizontalDistanceSqr() > NEAR_ZERO_DELTA_MOVEMENT;
    }

    private boolean isMovingInWater() {
        return this.entity.isInWaterOrBubble() && this.isMoving();
    }

    private boolean isMovingOnLandOrInWater() {
        return (this.entity.isOnGround() || this.entity.isInWaterOrBubble()) && this.isMoving();
    }

    public void handleEntityEventAnimation(byte id) {
        if(id == JUMPING_EVENT_ID){
            this.startJumpAnimation();
        } else if(id == ATTACKING_EVENT_ID){
            this.startAttackAnimation();
        }
    }
}
