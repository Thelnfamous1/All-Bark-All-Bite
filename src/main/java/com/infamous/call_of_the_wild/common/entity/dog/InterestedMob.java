package com.infamous.call_of_the_wild.common.entity.dog;

import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.MutablePair;

public interface InterestedMob {

    default void tickInterest() {
        float interestedAngle = this.getInterestedAngles().right;
        this.setInterestedAngles(interestedAngle, this.isInterested() ?
                interestedAngle + (1.0F - interestedAngle) * 0.4F :
                interestedAngle + (0.0F - interestedAngle) * 0.4F);
    }

    default float getHeadRollAngle(float partialTicks){
        return Mth.lerp(partialTicks, this.getInterestedAngles().left, this.getInterestedAngles().right) * 0.15F * (float)Math.PI;
    }

    boolean isInterested();

    void setIsInterested(boolean isInterested);

    default void setInterestedAngles(float interestedAngleO, float interestedAngle){
        this.getInterestedAngles().setLeft(interestedAngleO);
        this.getInterestedAngles().setRight(interestedAngle);
    }

    MutablePair<Float, Float> getInterestedAngles();

}
