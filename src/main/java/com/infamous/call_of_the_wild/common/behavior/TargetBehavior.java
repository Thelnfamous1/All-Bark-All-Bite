package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.scores.Team;

import javax.annotation.Nullable;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public abstract class TargetBehavior<E extends Mob> extends Behavior<E> {
    private static final int EMPTY_REACH_CACHE = 0;
    private static final int CAN_REACH_CACHE = 1;
    private static final int CANT_REACH_CACHE = 2;
    protected final boolean mustSee;
    private final boolean mustReach;
    private int reachCache;
    private int reachCacheTime;
    private int unseenTicks;
    protected int unseenMemoryTicks;

    public TargetBehavior(boolean mustSee) {
        this(mustSee, false, 60);
    }

    @SuppressWarnings("unused")
    public TargetBehavior(boolean mustSee, boolean mustReach) {
        this(mustSee, mustReach, 60);
    }
    public TargetBehavior(boolean mustSee, boolean mustReach, int unseenMemoryTicks) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
        this.mustSee = mustSee;
        this.mustReach = mustReach;
        this.unseenMemoryTicks = unseenMemoryTicks;
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        Optional<LivingEntity> optionalTarget = GenericAi.getAttackTarget(mob);
        if(optionalTarget.isPresent()){
            LivingEntity target = optionalTarget.get();
            if (!mob.canAttack(target)) {
                return false;
            } else {
                Team team = mob.getTeam();
                Team team1 = target.getTeam();
                if (team != null && team1 == team) {
                    return false;
                } else {
                    double followDistance = this.getFollowDistance(mob);
                    if (mob.distanceToSqr(target) > followDistance * followDistance) {
                        return false;
                    } else {
                        if (this.mustSee) {
                            if (mob.getSensing().hasLineOfSight(target)) {
                                this.unseenTicks = 0;
                            } else if (++this.unseenTicks > AiUtil.reducedTickDelay(this.unseenMemoryTicks)) {
                                return false;
                            }
                        }

                        StartAttacking.setAttackTarget(mob, target);
                        return true;
                    }
                }
            }
        } else{
            return false;
        }
    }

    protected double getFollowDistance(E mob) {
        return mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        this.reachCache = EMPTY_REACH_CACHE;
        this.reachCacheTime = 0;
        this.unseenTicks = 0;
    }

    @Override
    protected void stop(ServerLevel level, E mob, long gameTime) {
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }

    @SuppressWarnings("SameParameterValue")
    protected boolean canAttack(E mob, @Nullable LivingEntity target, TargetingConditions conditions) {
        if (target == null) {
            return false;
        } else if (!conditions.test(mob, target)) {
            return false;
        } else if (!mob.isWithinRestriction(target.blockPosition())) {
            return false;
        } else {
            if (this.mustReach) {
                if (--this.reachCacheTime <= 0) {
                    this.reachCache = EMPTY_REACH_CACHE;
                }

                if (this.reachCache == EMPTY_REACH_CACHE) {
                    this.reachCache = this.canReach(mob, target) ? CAN_REACH_CACHE : CANT_REACH_CACHE;
                }

                return this.reachCache != CANT_REACH_CACHE;
            }

            return true;
        }
    }

    private boolean canReach(E mob, LivingEntity target) {
        this.reachCacheTime = AiUtil.reducedTickDelay(10 + mob.getRandom().nextInt(5));
        Path pathToTarget = mob.getNavigation().createPath(target, 0);
        if (pathToTarget == null) {
            return false;
        } else {
            Node endNode = pathToTarget.getEndNode();
            if (endNode == null) {
                return false;
            } else {
                int xDiff = endNode.x - target.getBlockX();
                int zDiff = endNode.z - target.getBlockZ();
                return (double)(xDiff * xDiff + zDiff * zDiff) <= 2.25D;
            }
        }
    }

}
