package com.infamous.call_of_the_wild.common.util;

import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.animal.Wolf;

import java.util.List;
import java.util.Optional;

public class PackAi {
    public static boolean hasFollowers(LivingEntity mob) {
        return getPackSize(mob) > 1;
    }

    public static int getPackSize(LivingEntity mob) {
        return mob.getBrain().getMemory(COTWMemoryModuleTypes.PACK_SIZE.get()).orElse(1);
    }

    public static Optional<LivingEntity> getLeader(LivingEntity mob) {
        return mob.getBrain().getMemory(COTWMemoryModuleTypes.PACK_LEADER.get());
    }

    public static void stopFollowing(LivingEntity mob) {
        getLeader(mob).ifPresent(leader -> {
            removeFollower(leader);
            eraseLeader(mob);
        });
    }

    private static void setPackSize(LivingEntity mob, int packSize) {
        mob.getBrain().setMemory(COTWMemoryModuleTypes.PACK_SIZE.get(), Math.max(packSize, 1));
    }

    private static void removeFollower(LivingEntity mob) {
        setPackSize(mob, getPackSize(mob) - 1);
    }

    private static void eraseLeader(LivingEntity mob) {
        mob.getBrain().eraseMemory(COTWMemoryModuleTypes.PACK_LEADER.get());
    }

    public static boolean isFollower(LivingEntity mob) {
        Optional<LivingEntity> leader = getLeader(mob);
        return leader.isPresent() && leader.get().isAlive();
    }

    public static void startFollowing(LivingEntity mob, LivingEntity leader) {
        mob.getBrain().setMemory(COTWMemoryModuleTypes.PACK_LEADER.get(), leader);
        setPackSize(leader, getPackSize(leader) + 1);
    }

    public static boolean canFollow(LivingEntity mob, LivingEntity other, int otherMaxPackSize) {
        return AiUtil.canBeConsideredAnAlly(mob, other) && isIndependent(other, otherMaxPackSize) && canBeFollowed(other, otherMaxPackSize);
    }

    public static boolean isIndependent(LivingEntity mob, int maxPackSize) {
        return canBeFollowed(mob, maxPackSize) || !isFollower(mob);
    }

    public static boolean canBeFollowed(LivingEntity mob, int maxPackSize) {
        return hasFollowers(mob) && getPackSize(mob) < maxPackSize;
    }

    public static boolean canLead(LivingEntity leader, LivingEntity other, int otherMaxPackSize) {
        return AiUtil.canBeConsideredAnAlly(leader, other) && isIndependent(other, otherMaxPackSize) && !isFollower(other);
    }

    public static void pathToLeader(LivingEntity mob, float speedModifier, int closeEnough) {
        getLeader(mob).ifPresent(leader -> BehaviorUtils.setWalkAndLookTargetMemories(mob, leader, speedModifier, closeEnough));
    }

    /**
     * Called by {@link com.infamous.call_of_the_wild.common.entity.dog.WolfAi#updateActivity(Wolf)}
     */
    public static void updatePack(LivingEntity mob, int intervalTicks) {
        if (hasFollowers(mob) && mob.level.random.nextInt(intervalTicks) == 1) {
            List<LivingEntity> nearbyKin = GenericAi.getNearbyKin(mob);
            if (nearbyKin.size() <= 1) {
                setPackSize(mob, 1);
            }
        }
    }

}
