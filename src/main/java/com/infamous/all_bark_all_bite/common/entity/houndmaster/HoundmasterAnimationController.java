package com.infamous.all_bark_all_bite.common.entity.houndmaster;

import com.infamous.all_bark_all_bite.common.entity.EntityAnimationController;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.AbstractIllager;

public class HoundmasterAnimationController extends EntityAnimationController<Houndmaster> {
    public final AnimationState whistleAnimationState = new AnimationState();
    private final EntityDataAccessor<Boolean> whistling;

    public HoundmasterAnimationController(Houndmaster entity, EntityDataAccessor<Boolean> whistling, EntityDataAccessor<Pose> entityPose) {
        super(entity, entityPose);
        this.whistling = whistling;
    }

    @Override
    public void onSyncedDataUpdatedAnimations(EntityDataAccessor<?> dataAccessor) {
        if(this.whistling.equals(dataAccessor)){
            if(this.entity.isWhistling()){
                this.whistleAnimationState.startIfStopped(this.entity.tickCount);
                this.stopAllNonePoseAnimations();
            } else{
                this.whistleAnimationState.stop();
            }
        }
        super.onSyncedDataUpdatedAnimations(dataAccessor);
    }

    @Override
    public void tickAnimations() {
        this.tickBaby();
        if (this.entity.getArmPose() == AbstractIllager.IllagerArmPose.NEUTRAL) {
            this.tickBasicAnimations();
        } else{
            this.stopAllNonePoseAnimations();
        }
    }
}
