package com.infamous.call_of_the_wild.common.entity;

import com.infamous.call_of_the_wild.common.util.MiscUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;

public interface ShakingMob {

    byte START_SHAKING_ID = 8;
    byte STOP_SHAKING_ID = 56;
    float MAX_SHAKE_TIME_IN_SECONDS = 2.0F;
    float SECONDS_PER_TICK = 0.05F;
    float SECONDS_AFTER_START_TO_SPLASH = 0.4F;

    default PathfinderMob cast(){
        return (PathfinderMob) this;
    }

    default void tickShaking(){
        PathfinderMob self = this.cast();
        if (self.isInWaterRainOrBubble() && !self.level.isClientSide) {
            this.setIsWet(true);
            if (this.isShaking() && !self.level.isClientSide) {
                self.level.broadcastEntityEvent(self, STOP_SHAKING_ID);
                this.setIsShaking(false);
                this.setShakeAnims(0.0F, 0.0F);
            }
        } else if ((this.isWet() || this.isShaking()) && this.isShaking()) {
            if (this.getShakeAnims().right == 0.0F) {
                this.playShakeSound();
                self.gameEvent(GameEvent.ENTITY_SHAKE);
            }

            this.setShakeAnims(this.getShakeAnims().right, this.getShakeAnims().right + SECONDS_PER_TICK);
            if (this.getShakeAnims().left >= MAX_SHAKE_TIME_IN_SECONDS) {
                if(!self.level.isClientSide){
                    self.level.broadcastEntityEvent(self, STOP_SHAKING_ID);
                    this.setIsWet(false);
                    this.setIsShaking(false);
                    this.setShakeAnims(0.0F, 0.0F);
                }
            }
            else if (this.getShakeAnims().right > SECONDS_AFTER_START_TO_SPLASH) {
                int numParticles = (int)(Mth.sin((this.getShakeAnims().right - SECONDS_AFTER_START_TO_SPLASH) * (float)Math.PI) * 7.0F);
                Vec3 deltaMovement = self.getDeltaMovement();
                MiscUtil.addParticlesAroundSelf(self, ParticleTypes.SPLASH, numParticles, deltaMovement.x, deltaMovement.y, deltaMovement.z, 0.5D, 0.8D);
            }
        }
    }

    default void aiStepShaking(){
        PathfinderMob self = this.cast();
        if (!self.level.isClientSide && this.isWet() && !this.isShaking() && !self.isPathFinding() && self.isOnGround()) {
            this.setIsShaking(true);
            this.setShakeAnims(0.0F, 0.0F);
            self.level.broadcastEntityEvent(self, START_SHAKING_ID);
        }
    }

    default void handleShakingEvent(byte id){
        if (id == START_SHAKING_ID) {
            this.setShakeAnims(0.0F, 0.0F);
        } else if (id == STOP_SHAKING_ID) {
            this.setShakeAnims(0.0F, 0.0F);
        }
    }

    default float getBodyRollAngle(float partialTick, float initialZRot) {
        float bodyRollAngle = (Mth.lerp(partialTick, this.getShakeAnims().left, this.getShakeAnims().right) + initialZRot) / 1.8F;
        if (bodyRollAngle < 0.0F) {
            bodyRollAngle = 0.0F;
        } else if (bodyRollAngle > 1.0F) {
            bodyRollAngle = 1.0F;
        }

        return Mth.sin(bodyRollAngle * (float)Math.PI) * Mth.sin(bodyRollAngle * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
    }

    default void dieShaking(){
        this.setIsWet(false);
        this.setIsShaking(false);
        this.setShakeAnims(0.0F, 0.0F);
    }

    default float getWetShade(float partialTicks) {
        return Math.min(0.5F + Mth.lerp(partialTicks, this.getShakeAnims().left, this.getShakeAnims().right) / MAX_SHAKE_TIME_IN_SECONDS * 0.5F, 1.0F);
    }

    boolean isWet();

    void setIsWet(boolean isWet);

    boolean isShaking();

    void setIsShaking(boolean isShaking);

    default void setShakeAnims(float shakeAnimO, float shakeAnim){
        this.getShakeAnims().setLeft(shakeAnimO);
        this.getShakeAnims().setRight(shakeAnim);
    }

    MutablePair<Float, Float> getShakeAnims();

    void playShakeSound();

}
