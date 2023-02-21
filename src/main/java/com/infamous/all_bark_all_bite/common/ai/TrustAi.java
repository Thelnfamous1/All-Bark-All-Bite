package com.infamous.all_bark_all_bite.common.ai;

import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Wolf;

import java.util.Optional;

public class TrustAi {

    public static Optional<LivingEntity> getLikedPlayer(LivingEntity wolf){
        return BehaviorUtils.getLivingEntityFromUUIDMemory(wolf, MemoryModuleType.LIKED_PLAYER);
    }

    public static void setLikedPlayer(LivingEntity wolf, LivingEntity likedPlayer){
        wolf.getBrain().setMemory(MemoryModuleType.LIKED_PLAYER, likedPlayer.getUUID());
    }

    public static void incrementTrust(LivingEntity wolf, int toAdd){
        setTrust(wolf, getTrust(wolf) + toAdd);
    }

    public static void decrementTrust(LivingEntity wolf, int toSubtract){
        setTrust(wolf, getTrust(wolf) - toSubtract);
    }

    public static void setTrust(LivingEntity wolf, int trust) {
        wolf.getBrain().setMemory(ABABMemoryModuleTypes.TRUST.get(), trust);
    }

    public static int getTrust(LivingEntity wolf){
        return wolf.getBrain().getMemory(ABABMemoryModuleTypes.TRUST.get()).orElse(0);
    }

    public static void eraseTrust(LivingEntity wolf){
        wolf.getBrain().eraseMemory(ABABMemoryModuleTypes.TRUST.get());
    }

    public static void erasedLikedPlayer(LivingEntity wolf){
        wolf.getBrain().eraseMemory(MemoryModuleType.LIKED_PLAYER);
    }

    public static void setMaxTrust(LivingEntity wolf, int maxTrust) {
        wolf.getBrain().setMemory(ABABMemoryModuleTypes.MAX_TRUST.get(), maxTrust);
    }

    public static int getMaxTrust(LivingEntity wolf) {
        return wolf.getBrain().getMemory(ABABMemoryModuleTypes.MAX_TRUST.get()).orElse(0);
    }

    public static boolean likes(Wolf wolf, LivingEntity player) {
        return getLikedPlayer(wolf).orElse(null) == player;
    }

    public static void eraseMaxTrust(LivingEntity wolf) {
        wolf.getBrain().eraseMemory(ABABMemoryModuleTypes.MAX_TRUST.get());
    }

    public static boolean isFullyTrusting(Wolf wolf) {
        return getTrust(wolf) >= getMaxTrust(wolf);
    }

    public static void setRandomMaxTrust(LivingEntity wolf, int maxTrust) {
        setMaxTrust(wolf, wolf.getRandom().nextInt(maxTrust) + 1);
    }
}
