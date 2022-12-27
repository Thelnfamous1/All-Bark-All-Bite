package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
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

import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public class FollowPackLeader<E extends LivingEntity> extends Behavior<E> {
    public static final int INTERVAL_TICKS = 200;
    private final UniformInt followRange;
    private final Function<LivingEntity, Integer> maxPackSize;
    private int timeToRecalcPath;
    private long lastCheckTimestamp;
    private final float speedModifier;

    public FollowPackLeader(UniformInt followRange, Function<LivingEntity, Integer> maxPackSize, float speedModifier) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.PACK_LEADER.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.PACK_SIZE.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_KIN.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
        this.followRange = followRange;
        this.maxPackSize = maxPackSize;
        this.speedModifier = speedModifier;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (PackAi.hasFollowers(mob)) {
            return false;
        } else if (PackAi.isFollower(mob)) {
            return true;
        } else if (level.getGameTime() - this.lastCheckTimestamp < INTERVAL_TICKS) {
            return false;
        } else {
            this.lastCheckTimestamp = level.getGameTime();

            // Find a suitable leader, or promote self to leader
            LivingEntity leader = GenericAi.getNearbyVisibleAdults(mob).stream()
                    .filter(le -> PackAi.canFollow(mob, le, this.maxPackSize.apply(le)))
                    .findAny()
                    .orElse(mob);
            MiscUtil.sendParticlesAroundSelf(level, leader, ParticleTypes.ANGRY_VILLAGER, leader.getEyeHeight(),  10, 0.2D);

            // Tell nearby kin to follow leader or self if possible
            this.addFollowers(level, leader, GenericAi.getNearbyVisibleKin(mob).stream(), this.maxPackSize.apply(leader));

            return PackAi.isFollower(mob);
        }
    }

    private void addFollowers(ServerLevel level, LivingEntity leader, Stream<LivingEntity> stream, int maxPackSize) {
        stream.filter(le -> le != leader && PackAi.canLead(leader, le, this.maxPackSize.apply(le)))
                .limit(Math.max(maxPackSize - PackAi.getPackSize(leader), 0))
                .forEach(le -> {
                    PackAi.startFollowing(le, leader);
                    MiscUtil.sendParticlesAroundSelf(level, le, ParticleTypes.HAPPY_VILLAGER, le.getEyeHeight(),  10, 0.2D);
                });
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void tick(ServerLevel level, E mob, long gameTime) {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = AiUtil.reducedTickDelay(10);
            PackAi.pathToLeader(mob, this.speedModifier, this.followRange.getMinValue() - 1);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        if(PackAi.isFollower(mob)){
            LivingEntity leader = PackAi.getLeader(mob).get();
            return mob.closerThan(leader, this.followRange.getMaxValue() + 1) && !mob.closerThan(leader, this.followRange.getMinValue());
        }
        return false;
    }

    @Override
    public void stop(ServerLevel level, E mob, long gameTime) {
        PackAi.stopFollowing(mob);
    }

}
