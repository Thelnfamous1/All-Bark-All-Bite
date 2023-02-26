package com.infamous.all_bark_all_bite.common.util.ai;

import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.logic.entity_manager.MultiEntityManager;
import com.infamous.all_bark_all_bite.common.logic.entity_manager.SingleEntityManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class PackAi {
    public static boolean hasFollowers(LivingEntity mob) {
        return getPackSize(mob) > 0;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static int getPackSize(LivingEntity mob) {
        return getFollowerManager(mob).get().size();
    }

    public static Optional<MultiEntityManager> getFollowerManager(LivingEntity mob) {
        Brain<?> brain = mob.getBrain();
        if(!brain.hasMemoryValue(ABABMemoryModuleTypes.FOLLOWERS.get())){
            brain.setMemory(ABABMemoryModuleTypes.FOLLOWERS.get(), new MultiEntityManager(Collections.emptyList()));
        }

        return brain.getMemory(ABABMemoryModuleTypes.FOLLOWERS.get());
    }

    public static Optional<SingleEntityManager> getLeaderManager(LivingEntity mob) {
        Brain<?> brain = mob.getBrain();
        if(!brain.hasMemoryValue(ABABMemoryModuleTypes.LEADER.get())){
            brain.setMemory(ABABMemoryModuleTypes.LEADER.get(), new SingleEntityManager());
        }

        return brain.getMemory(ABABMemoryModuleTypes.LEADER.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static Optional<UUID> getLeaderUUID(LivingEntity mob){
        return getLeaderManager(mob).get().getEntityUUID();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static Optional<LivingEntity> getLeader(LivingEntity mob) {
        return getLeaderManager(mob).get().getEntity().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast);
    }

    public static void stopFollowing(LivingEntity mob, LivingEntity leader) {
        removeFollower(leader, mob);
        eraseLeader(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void removeFollower(LivingEntity leader, LivingEntity mob) {
        getFollowerManager(leader).get().remove(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void eraseLeader(LivingEntity mob) {
        getLeaderManager(mob).get().erase();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static boolean isFollower(LivingEntity mob) {
        return getLeaderManager(mob).get().hasEntity();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void startFollowing(LivingEntity mob, LivingEntity leader) {
        setLeader(mob, leader);
        getFollowerManager(leader).get().add(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void setLeader(LivingEntity mob, LivingEntity leader) {
        getLeaderManager(mob).get().set(leader);
    }

    public static boolean canFollow(LivingEntity mob, LivingEntity other) {
        return AiUtil.isSameTypeAndFriendly(mob, other) && (isJoinableLeader(other) || isLoner(other));
    }

    public static boolean isJoinableLeader(LivingEntity mob) {
        return hasFollowers(mob) && getPackSize(mob) < PackAi.getMaxPackSize(mob);
    }

    public static boolean canLead(LivingEntity leader, LivingEntity other) {
        return AiUtil.isSameTypeAndFriendly(leader, other) && isLoner(other);
    }

    public static void pathToLeader(LivingEntity mob, float speedModifier, int closeEnough) {
        getLeader(mob).ifPresent(leader -> AiUtil.setWalkAndLookTargetMemories(mob, leader, speedModifier, closeEnough));
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
