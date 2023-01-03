package com.infamous.call_of_the_wild.common.behavior.pack;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.infamous.call_of_the_wild.common.util.MiscUtil;
import com.infamous.call_of_the_wild.common.util.PackAi;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public class FollowPackLeader<E extends LivingEntity> extends Behavior<E> {
    public static final int INTERVAL_TICKS = 200;
    private final UniformInt followRange;
    private long lastCheckTimestamp;
    private final float speedModifier;

    public FollowPackLeader(UniformInt followRange, float speedModifier) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.LEADER.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.FOLLOWERS.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
        this.followRange = followRange;
        this.speedModifier = speedModifier;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (PackAi.hasFollowers(mob)) {
            return false;
        } else if (PackAi.isFollower(mob)) {
            return this.wantsToFollowLeader(mob);
        } else if (level.getGameTime() - this.lastCheckTimestamp < INTERVAL_TICKS) {
            return false;
        } else {
            this.lastCheckTimestamp = level.getGameTime();

            this.joinOrCreatePack(level, mob);

            return PackAi.isFollower(mob);
        }
    }

    private boolean wantsToFollowLeader(E mob) {
        Optional<LivingEntity> leader = PackAi.getLeader(mob);
        return leader.isPresent() && mob.closerThan(leader.get(), this.followRange.getMaxValue() + 1) && !mob.closerThan(leader.get(), this.followRange.getMinValue());
    }

    private void joinOrCreatePack(ServerLevel level, E mob) {
        List<LivingEntity> nearbyVisibleAdults = GenericAi.getNearbyVisibleAdults(mob);

        // Find a suitable leader, or promote self to leader
        LivingEntity leader = this.findLeader(level, mob, nearbyVisibleAdults.stream());

        // Tell nearby allies to follow leader
        this.addFollowers(level, leader, nearbyVisibleAdults.stream());
    }

    private LivingEntity findLeader(ServerLevel level, E mob, Stream<LivingEntity> stream) {
        LivingEntity leader = stream
                .filter(le -> PackAi.canFollow(mob, le))
                .findAny()
                .orElse(mob);

        MiscUtil.sendParticlesAroundSelf(level, leader, ParticleTypes.FLAME, leader.getEyeHeight(),  10, 0.2D);
        if(leader != mob){
            PackAi.startFollowing(mob, leader);
            MiscUtil.sendParticlesAroundSelf(level, mob, ParticleTypes.SOUL_FIRE_FLAME, mob.getEyeHeight(),  10, 0.2D);
        }
        return leader;
    }

    private void addFollowers(ServerLevel level, LivingEntity leader, Stream<LivingEntity> stream) {
        stream.filter(le -> le != leader && PackAi.canLead(leader, le))
                .limit(Math.max(PackAi.getMaxPackSize(leader) - PackAi.getPackSize(leader), 0))
                .forEach(le -> {
                    PackAi.startFollowing(le, leader);
                    MiscUtil.sendParticlesAroundSelf(level, le, ParticleTypes.SOUL_FIRE_FLAME, le.getEyeHeight(),  10, 0.2D);
                });
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        PackAi.pathToLeader(mob, this.speedModifier, this.followRange.getMinValue() - 1);
    }

}
