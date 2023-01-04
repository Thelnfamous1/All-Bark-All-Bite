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

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("NullableProblems")
public class ValidateFollowers extends Behavior<LivingEntity> {

    private static final int CLOSE_ENOUGH_TO_RECALL = 64;
    private long lastCheckTimestamp;

    public ValidateFollowers() {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.FOLLOWERS.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity leader) {
        if(PackAi.hasFollowers(leader)){
            if (this.lastCheckTimestamp != 0 && level.getGameTime() - this.lastCheckTimestamp < FollowPackLeader.INTERVAL_TICKS) {
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
        Set<UUID> followers = PackAi.getFollowerUUIDs(leader).get();
        for (Iterator<UUID> itr = followers.iterator(); itr.hasNext();) {
            UUID followerUUID = itr.next();
            if(followerUUID.equals(leader.getUUID())) continue; // ignore self
            Optional<LivingEntity> follower = AiUtil.getLivingEntityFromUUID(level, followerUUID);
            if(follower.isPresent()){
                LivingEntity f = follower.get();
                if(!f.isAlive() || f.level != leader.level || !AiUtil.canBeConsideredAnAlly(leader, f) || !f.closerThan(leader, CLOSE_ENOUGH_TO_RECALL)){
                    PackAi.stopFollowing(f, leader);
                    MiscUtil.sendParticlesAroundSelf((ServerLevel) f.level, f, ParticleTypes.SMOKE, f.getEyeHeight(),  10, 0.2D);
                }
            } else{
                itr.remove();
            }
        }
    }
}
