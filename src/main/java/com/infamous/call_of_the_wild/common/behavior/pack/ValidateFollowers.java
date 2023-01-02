package com.infamous.call_of_the_wild.common.behavior.pack;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.PackAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

@SuppressWarnings("NullableProblems")
public class ValidateFollowers extends Behavior<LivingEntity> {

    private long lastCheckTimestamp;

    public ValidateFollowers() {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.FOLLOWERS.get(), MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity mob) {
        if(PackAi.hasFollowers(mob)){
            if (level.getGameTime() - this.lastCheckTimestamp < FollowPackLeader.INTERVAL_TICKS) {
                return false;
            } else {
                this.lastCheckTimestamp = level.getGameTime();
                return true;
            }
        } else {
            return false;
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, LivingEntity leader, long gameTime) {
        PackAi.getFollowers(leader).get().forEach(follower -> {
            if(!follower.isAlive() || follower.level != leader.level || !AiUtil.canBeConsideredAnAlly(leader, follower)){
                PackAi.stopFollowing(follower, leader);
            }
        });
    }
}
