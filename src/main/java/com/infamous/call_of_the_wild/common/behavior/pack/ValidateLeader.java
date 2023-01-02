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

import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class ValidateLeader extends Behavior<LivingEntity> {
    private long lastCheckTimestamp;

    public ValidateLeader() {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.LEADER.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity follower) {
        if (level.getGameTime() - this.lastCheckTimestamp < FollowPackLeader.INTERVAL_TICKS) {
            return false;
        } else {
            this.lastCheckTimestamp = level.getGameTime();
            return true;
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, LivingEntity follower, long gameTime) {
        Optional<LivingEntity> packLeader = PackAi.getLeader(follower);
        if(packLeader.isPresent()){
            LivingEntity leader = packLeader.get();
            if(!leader.isAlive() || PackAi.isFollower(leader) || !AiUtil.canBeConsideredAnAlly(follower, leader)){
                PackAi.stopFollowing(follower, leader);
                MiscUtil.sendParticlesAroundSelf(level, follower, ParticleTypes.SMOKE, follower.getEyeHeight(),  10, 0.2D);
            } else{
                PackAi.getFollowers(leader).get().add(follower);
            }
        } else{
            PackAi.eraseLeader(follower);
        }
    }
}
