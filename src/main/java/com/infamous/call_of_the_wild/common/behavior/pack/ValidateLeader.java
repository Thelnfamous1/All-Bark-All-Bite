package com.infamous.call_of_the_wild.common.behavior.pack;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
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
    private static final int CLOSE_ENOUGH_TO_BE_RECALLED = 64;
    private long lastCheckTimestamp;

    public ValidateLeader() {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.LEADER.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity follower) {
        if (this.lastCheckTimestamp != 0 && level.getGameTime() - this.lastCheckTimestamp < FollowPackLeader.INTERVAL_TICKS) {
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
            if(!leader.isAlive() || PackAi.isFollower(leader) || !AiUtil.canBeConsideredAnAlly(follower, leader) || !follower.closerThan(leader, CLOSE_ENOUGH_TO_BE_RECALLED)){
                PackAi.stopFollowing(follower, leader);
                MiscUtil.sendParticlesAroundSelf(level, follower, ParticleTypes.SMOKE, follower.getEyeHeight(),  10, 0.2D);
            } else{
                PackAi.getFollowerUUIDs(leader).get().add(follower.getUUID()); // re-add the follower to the leader's followers if not already present
            }
        } else{
            PackAi.eraseLeader(follower);
        }
    }
}
