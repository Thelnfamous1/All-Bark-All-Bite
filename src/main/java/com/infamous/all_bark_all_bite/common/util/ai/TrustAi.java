package com.infamous.all_bark_all_bite.common.util.ai;

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

    public static boolean isLikedBy(Wolf wolf, LivingEntity player) {
        return wolf.getBrain().getMemory(MemoryModuleType.LIKED_PLAYER).filter(uuid -> uuid.equals(player.getUUID())).isPresent();
    }

}
