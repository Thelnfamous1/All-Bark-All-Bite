package com.infamous.all_bark_all_bite.common.behavior.pet;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SitWhenOrderedTo extends Behavior<TamableAnimal> {

    public SitWhenOrderedTo() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TamableAnimal tamable) {
        if (!tamable.isTame()) {
            return false;
        } else if (tamable.isInWaterOrBubble()) {
            return false;
        } else if (!tamable.onGround()) {
            return false;
        } else {
            LivingEntity owner = tamable.getOwner();
            if (owner == null) {
                return true;
            } else {
                return (!(tamable.distanceToSqr(owner) < Mth.square(ABABConfig.petTeleportDistanceTrigger.get())) || owner.getLastHurtByMob() == null) && tamable.isOrderedToSit();
            }
        }
    }

    @Override
    protected void start(ServerLevel level, TamableAnimal tamable, long gameTime) {
        GenericAi.stopWalking(tamable);
        tamable.setInSittingPose(true);
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, TamableAnimal tamable, long gameTime) {
        return tamable.isOrderedToSit();
    }

    @Override
    protected void stop(ServerLevel level, TamableAnimal tamable, long gameTime) {
        tamable.setInSittingPose(false);
    }
}
