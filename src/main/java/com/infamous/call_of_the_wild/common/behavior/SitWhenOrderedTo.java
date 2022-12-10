package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.util.AiHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

@SuppressWarnings("NullableProblems")
public class SitWhenOrderedTo extends Behavior<TamableAnimal> {
    private static final int MAX_DISTANCE = 12;

    public SitWhenOrderedTo() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TamableAnimal tamable) {
        if (!tamable.isTame()) {
            return false;
        } else if (tamable.isInWaterOrBubble()) {
            return false;
        } else if (!tamable.isOnGround()) {
            return false;
        } else {
            LivingEntity owner = tamable.getOwner();
            if (owner == null) {
                return true;
            } else {
                return (!(tamable.distanceToSqr(owner) < MAX_DISTANCE * MAX_DISTANCE) || owner.getLastHurtByMob() == null) && tamable.isOrderedToSit();
            }
        }
    }

    @Override
    protected void start(ServerLevel level, TamableAnimal tamable, long gameTime) {
        AiHelper.stopWalking(tamable);
        tamable.setInSittingPose(true);
    }

    @Override
    protected boolean timedOut(long p_22537_) {
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
