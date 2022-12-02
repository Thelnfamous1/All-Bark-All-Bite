package com.infamous.call_of_the_wild.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;

public interface ShakingMob<T extends Animal> {

    byte START_SHAKING_ID = 8;
    byte STOP_SHAKING_ID = 56;

    default void tickShaking(T animal){
        if (animal.isInWaterRainOrBubble()) {
            this.setIsWet(true);
            if (this.isShaking() && !animal.level.isClientSide) {
                animal.level.broadcastEntityEvent(animal, STOP_SHAKING_ID);
                this.cancelShake();
            }
        } else if ((this.isWet() || this.isShaking()) && this.isShaking()) {
            if (this.getShakeAnims().right == 0.0F) {
                this.playShakeSound();
                animal.gameEvent(GameEvent.ENTITY_SHAKE);
            }

            this.setShakeAnims(this.getShakeAnims().right, this.getShakeAnims().right + 0.05F);
            if (this.getShakeAnims().left >= 2.0F) {
                this.setIsWet(false);
                this.setIsShaking(false);
                this.setShakeAnims(0.0F, 0.0F);
            }

            if (this.getShakeAnims().right > 0.4F) {
                float y = (float)animal.getY();
                int i = (int)(Mth.sin((this.getShakeAnims().right - 0.4F) * (float)Math.PI) * 7.0F);
                Vec3 deltaMovement = animal.getDeltaMovement();

                for(int j = 0; j < i; ++j) {
                    float f1 = (animal.getRandom().nextFloat() * 2.0F - 1.0F) * animal.getBbWidth() * 0.5F;
                    float f2 = (animal.getRandom().nextFloat() * 2.0F - 1.0F) * animal.getBbWidth() * 0.5F;
                    animal.level.addParticle(ParticleTypes.SPLASH, animal.getX() + (double)f1, (double)(y + 0.8F), animal.getZ() + (double)f2, deltaMovement.x, deltaMovement.y, deltaMovement.z);
                }
            }
        }
    }

    default void aiStepShaking(T animal){
        if (!animal.level.isClientSide && this.isWet() && !this.isShaking() && !animal.isPathFinding() && animal.isOnGround()) {
            this.setIsShaking(true);
            this.setShakeAnims(0.0F, 0.0F);
            animal.level.broadcastEntityEvent(animal, START_SHAKING_ID);
        }
    }

    default boolean handleShakingEvent(byte id){
        if (id == START_SHAKING_ID) {
            this.setIsShaking(true);
            this.setShakeAnims(0.0F, 0.0F);
            return true;
        } else if (id == STOP_SHAKING_ID) {
            this.cancelShake();
            return true;
        }
        return false;
    }

    default void cancelShake(){
        this.setIsShaking(false);
        this.setShakeAnims(0.0F, 0.0F);
    }

    default void dieShaking(){
        this.setIsWet(false);
        this.setIsShaking(false);
        this.setShakeAnims(0.0F, 0.0F);
    }

    default float getWetShade(float p_30447_) {
        return Math.min(0.5F + Mth.lerp(p_30447_, this.getShakeAnims().left, this.getShakeAnims().right) / 2.0F * 0.5F, 1.0F);
    }

    default float getBodyRollAngle(float partialTicks, float p_30434_) {
        float lerpShakeAnim = (Mth.lerp(partialTicks, this.getShakeAnims().left, this.getShakeAnims().right) + p_30434_) / 1.8F;
        if (lerpShakeAnim < 0.0F) {
            lerpShakeAnim = 0.0F;
        } else if (lerpShakeAnim > 1.0F) {
            lerpShakeAnim = 1.0F;
        }

        return Mth.sin(lerpShakeAnim * (float)Math.PI) * Mth.sin(lerpShakeAnim * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
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
