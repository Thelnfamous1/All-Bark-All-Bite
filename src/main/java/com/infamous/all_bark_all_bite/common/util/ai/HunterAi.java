package com.infamous.all_bark_all_bite.common.util.ai;

import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;
import java.util.Optional;

public class HunterAi {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasAnyoneNearbyHuntedRecently(LivingEntity mob, List<? extends LivingEntity> nearby) {
       return hasHuntedRecently(mob) || nearby.stream().anyMatch(HunterAi::hasHuntedRecently);
    }

    public static boolean hasHuntedRecently(LivingEntity mob) {
        return mob.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY);
    }

    public static void setHuntedRecently(LivingEntity mob, int huntCooldownInTicks) {
       mob.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, huntCooldownInTicks);
    }

    public static void broadcastHuntedRecently(UniformInt huntCooldown, List<? extends LivingEntity> alertables) {
       alertables.forEach(alertable -> setHuntedRecently(alertable, huntCooldown.sample(alertable.getRandom())));
    }

    public static Optional<LivingEntity> getNearestVisibleHuntable(LivingEntity mob){
        return mob.getBrain().getMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get());
    }

    public static Optional<LivingEntity> getHuntTarget(LivingEntity mob){
        return mob.getBrain().getMemory(ABABMemoryModuleTypes.HUNT_TARGET.get());
    }

    public static void stopHunting(LivingEntity mob) {
        mob.getBrain().eraseMemory(ABABMemoryModuleTypes.HUNT_TARGET.get());
    }

    public static void broadcastHuntTarget(List<? extends LivingEntity> alertables, LivingEntity target) {
        alertables.forEach(alertable -> setHuntTargetIfCloserThanCurrent(alertable, target));
    }

    private static void setHuntTargetIfCloserThanCurrent(LivingEntity mob, LivingEntity target) {
        Optional<LivingEntity> huntTarget = mob.getBrain().getMemory(ABABMemoryModuleTypes.HUNT_TARGET.get());
        LivingEntity nearestTarget = BehaviorUtils.getNearestTarget(mob, huntTarget, target);
        setHuntTarget(mob, nearestTarget);
    }

    public static void setHuntTarget(LivingEntity mob, LivingEntity target) {
        mob.getBrain().setMemory(ABABMemoryModuleTypes.HUNT_TARGET.get(), target);
    }

}
