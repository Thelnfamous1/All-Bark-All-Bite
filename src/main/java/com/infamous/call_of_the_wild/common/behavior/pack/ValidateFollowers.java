package com.infamous.call_of_the_wild.common.behavior.pack;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.MiscUtil;
import com.infamous.call_of_the_wild.common.util.PackAi;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Set;

@SuppressWarnings("NullableProblems")
public class ValidateFollowers extends Behavior<LivingEntity> {

    private static final int CLOSE_ENOUGH_TO_RECALL = 64;
    private long lastCheckTimestamp;

    public ValidateFollowers() {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.FOLLOWERS.get(), MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity leader) {
        if(PackAi.hasFollowers(leader)){
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
        Set<LivingEntity> followers = PackAi.getFollowers(leader).get();
        for(LivingEntity follower : followers){
            if(follower == leader) continue; // ignore self
            if(!follower.isAlive() || follower.level != leader.level || !AiUtil.canBeConsideredAnAlly(leader, follower) || !follower.closerThan(leader, CLOSE_ENOUGH_TO_RECALL)){
                PackAi.stopFollowing(follower, leader);
                MiscUtil.sendParticlesAroundSelf((ServerLevel) follower.level, follower, ParticleTypes.SMOKE, follower.getEyeHeight(),  10, 0.2D);
            }
        }
    }
}
