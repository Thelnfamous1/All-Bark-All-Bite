package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.COTWUtil;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.infamous.call_of_the_wild.common.util.PackAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Wolf;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public class FollowPackLeader<E extends LivingEntity> extends Behavior<E> {
    private static final int INTERVAL_TICKS = 200;
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
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
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
                    .filter(le -> this.canFollow(mob, le))
                    .findAny()
                    .orElse(mob);

            // Tell nearby kin to follow leader or self if possible
            this.addFollowers(leader, COTWUtil.castStream(GenericAi.getNearbyVisibleKin(mob).stream()));

            return PackAi.isFollower(mob);
        }
    }

    private void addFollowers(LivingEntity leader, Stream<E> stream) {
        stream.filter(le -> le != leader && this.canLead(leader, le))
                .limit(Math.max(this.maxPackSize.apply(leader) - PackAi.getPackSize(leader), 0))
                .forEach(le -> PackAi.startFollowing(le, leader));
    }

    private boolean canFollow(LivingEntity mob, LivingEntity other) {
        return AiUtil.canBeConsideredAnAlly(mob, other) && this.isIndependent(other) && this.canBeFollowed(other);
    }

    private boolean isIndependent(LivingEntity mob) {
        return this.canBeFollowed(mob) || !PackAi.isFollower(mob);
    }

    private boolean canBeFollowed(LivingEntity mob) {
        return PackAi.hasFollowers(mob) && PackAi.getPackSize(mob) < this.maxPackSize.apply(mob);
    }

    private boolean canLead(LivingEntity leader, LivingEntity other) {
        return AiUtil.canBeConsideredAnAlly(leader, other) && this.isIndependent(other) && !PackAi.isFollower(other);
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void tick(ServerLevel level, E mob, long gameTime) {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = AiUtil.reducedTickDelay(10);
            this.pathToLeader(mob);
        }
    }

    private void pathToLeader(LivingEntity mob) {
        PackAi.getLeader(mob).ifPresent(leader -> BehaviorUtils.setWalkAndLookTargetMemories(mob, leader, this.speedModifier, this.followRange.getMinValue() - 1));
    }

    @Override
    public boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        Optional<LivingEntity> leader = PackAi.getLeader(mob);
        return PackAi.isFollower(mob) && leader.isPresent()
                && mob.closerThan(leader.get(), this.followRange.getMaxValue() + 1)
                && !mob.closerThan(leader.get(), this.followRange.getMinValue());
    }

    @Override
    public void stop(ServerLevel level, E mob, long gameTime) {
        PackAi.stopFollowing(mob);
    }

    /**
     * Called by {@link com.infamous.call_of_the_wild.common.entity.dog.WolfAi#updateActivity(Wolf)}
     */
    public static void updatePack(LivingEntity mob) {
        if (PackAi.hasFollowers(mob) && mob.level.random.nextInt(INTERVAL_TICKS) == 1) {
            List<LivingEntity> nearbyKin = GenericAi.getNearbyKin(mob);
            if (nearbyKin.size() <= 1) {
                PackAi.setPackSize(mob, 1);
            }
        }
    }
}
