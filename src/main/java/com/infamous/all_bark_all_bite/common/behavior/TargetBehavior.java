package com.infamous.all_bark_all_bite.common.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class TargetBehavior<E extends Mob> extends Behavior<E> {
    private static final int EMPTY_REACH_CACHE = 0;
    private static final int CAN_REACH_CACHE = 1;
    private static final int CANT_REACH_CACHE = 2;
    private static final double WITHIN_PATH_XZ = 1.5D;
    private final boolean mustReach;
    private int reachCache;
    private int reachCacheTime;
    public TargetBehavior(Map<MemoryModuleType<?>, MemoryStatus> additionalMemories, boolean mustReach) {
        super(ImmutableMap.<MemoryModuleType<?>, MemoryStatus>builder()
                .putAll(additionalMemories)
                .put(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)
                .put(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED)
                .build());
        this.mustReach = mustReach;
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        this.reachCache = EMPTY_REACH_CACHE;
        this.reachCacheTime = 0;
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
        this.reachCacheTime = 10 + mob.getRandom().nextInt(5);
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
                return (double)(xDiff * xDiff + zDiff * zDiff) <= Mth.square(WITHIN_PATH_XZ);
            }
        }
    }

}
