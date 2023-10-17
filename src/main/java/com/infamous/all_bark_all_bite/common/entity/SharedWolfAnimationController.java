package com.infamous.all_bark_all_bite.common.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class SharedWolfAnimationController extends EntityAnimationController<TamableAnimal> {
    private final EntityDataAccessor<Byte> tamableEntityFlags;
    public final AnimationState idleSitAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    private int idleSitDelayTicks;

    public SharedWolfAnimationController(TamableAnimal wolf, EntityDataAccessor<Byte> tamableEntityFlags, EntityDataAccessor<Pose> entityPose){
        super(wolf, entityPose);
        this.tamableEntityFlags = tamableEntityFlags;
    }

    @Override
    public void onSyncedDataUpdatedAnimations(EntityDataAccessor<?> dataAccessor) {
        if (this.tamableEntityFlags.equals(dataAccessor)) {
            if (this.entity.isInSittingPose()) {
                this.stopAllNonePoseAnimations();
                if(!this.idleSitAnimationState.isStarted()){
                    if (!this.sitAnimationState.isStarted()) {
                        this.idleSitDelayTicks = 4; // 0.2 seconds, same length as sit animation
                    }
                    this.sitAnimationState.startIfStopped(this.entity.tickCount);
                }
            } else {
                this.sitAnimationState.stop();
                this.idleSitAnimationState.stop();
                this.idleSitDelayTicks = 0;
            }
        } else{
            super.onSyncedDataUpdatedAnimations(dataAccessor);
        }
    }

    @Override
    public void aiStepAnimations() {
        super.aiStepAnimations();
        if (this.idleSitDelayTicks > 0) {
            --this.idleSitDelayTicks;
        }
    }

    @Override
    public void tickAnimations() {
        this.tickBaby();
        if (!this.entity.isInSittingPose()) {
            this.tickBasicAnimations();
        } else {
            if (this.idleSitDelayTicks == 0) {
                this.sitAnimationState.stop();
                this.idleSitAnimationState.startIfStopped(this.entity.tickCount);
            }
        }

        if (this.entity.getPose() == Pose.DIGGING) {
            this.clientDiggingParticles(this.digAnimationState);
        }
    }

    private void clientDiggingParticles(AnimationState animationState) {
        if ((float)animationState.getAccumulatedTime() < 4500.0F) {
            RandomSource random = this.entity.getRandom();
            BlockState blockStateOn = this.entity.getBlockStateOn();
            if (blockStateOn.getRenderShape() != RenderShape.INVISIBLE) {
                for(int particleCount = 0; particleCount < 10; ++particleCount) {
                    double x = this.entity.getX() + (double) Mth.randomBetween(random, -0.7F, 0.7F);
                    double y = this.entity.getY();
                    double z = this.entity.getZ() + (double)Mth.randomBetween(random, -0.7F, 0.7F);
                    this.entity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockStateOn), x, y, z, 0.0D, 0.0D, 0.0D);
                }
            }
        }

    }

}
