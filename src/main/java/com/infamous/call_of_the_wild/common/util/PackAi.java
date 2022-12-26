package com.infamous.call_of_the_wild.common.util;

import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class PackAi {
    public static boolean hasFollowers(LivingEntity mob) {
        return getPackSize(mob) > 1;
    }

    public static int getPackSize(LivingEntity mob) {
        return mob.getBrain().getMemory(COTWMemoryModuleTypes.PACK_SIZE.get()).orElse(0);
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

    public static void setPackSize(LivingEntity mob, int packSize) {
        mob.getBrain().setMemory(COTWMemoryModuleTypes.PACK_SIZE.get(), packSize);
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
}
