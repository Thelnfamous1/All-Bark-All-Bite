package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.*;

public class PackAi {
    public static boolean hasFollowers(LivingEntity mob) {
        return getPackSize(mob) > 1;
    }

    public static int getPackSize(LivingEntity mob) {
        return getFollowers(mob).orElse(ImmutableSet.of()).size();
    }

    public static Optional<Set<LivingEntity>> getFollowers(LivingEntity mob) {
        if(!mob.getBrain().hasMemoryValue(COTWMemoryModuleTypes.FOLLOWERS.get())){
            mob.getBrain().setMemory(COTWMemoryModuleTypes.FOLLOWERS.get(), Sets.newHashSet(mob));
        }
        return mob.getBrain().getMemory(COTWMemoryModuleTypes.FOLLOWERS.get());
    }

    public static Optional<UUID> getLeaderUUID(LivingEntity mob) {
        return mob.getBrain().getMemory(COTWMemoryModuleTypes.LEADER.get());
    }

    public static Optional<LivingEntity> getLeader(LivingEntity mob) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(mob, COTWMemoryModuleTypes.LEADER.get());
    }

    public static void stopFollowing(LivingEntity mob, LivingEntity leader) {
        removeFollower(leader, mob);
        eraseLeader(mob);
    }

    private static void removeFollower(LivingEntity leader, LivingEntity mob) {
        getFollowers(leader).ifPresent(followers -> followers.remove(mob));
    }

    public static void eraseLeader(LivingEntity mob) {
        mob.getBrain().eraseMemory(COTWMemoryModuleTypes.LEADER.get());
    }

    public static boolean isFollower(LivingEntity mob) {
        return getLeaderUUID(mob).isPresent();
    }

    public static void startFollowing(LivingEntity mob, LivingEntity leader) {
        setLeader(mob, leader);
        getFollowers(leader).ifPresent(followers -> followers.add(mob));
    }

    private static void setLeader(LivingEntity mob, LivingEntity leader) {
        mob.getBrain().setMemory(COTWMemoryModuleTypes.LEADER.get(), leader.getUUID());
    }

    public static boolean canFollow(LivingEntity mob, LivingEntity other) {
        return AiUtil.canBeConsideredAnAlly(mob, other) && (isJoinableLeader(other) || isLoner(other));
    }

    public static boolean isJoinableLeader(LivingEntity mob) {
        return hasFollowers(mob) && getPackSize(mob) < PackAi.getMaxPackSize(mob);
    }

    public static boolean canLead(LivingEntity leader, LivingEntity other) {
        return AiUtil.canBeConsideredAnAlly(leader, other) && isLoner(other);
    }

    public static void pathToLeader(LivingEntity mob, float speedModifier, int closeEnough) {
        getLeader(mob).ifPresent(leader -> BehaviorUtils.setWalkAndLookTargetMemories(mob, leader, speedModifier, closeEnough));
    }

    public static int getMaxPackSize(LivingEntity wolf) {
        return wolf instanceof Mob mob ? ForgeEventFactory.getMaxSpawnPackSize(mob) : 1;
    }

    public static boolean canAddToFollowers(LivingEntity leader, LivingEntity other) {
        return isJoinableLeader(leader) && canLead(leader, other);
    }

    public static boolean isLoner(LivingEntity mob){
        return !isFollower(mob) && !hasFollowers(mob);
    }
}
