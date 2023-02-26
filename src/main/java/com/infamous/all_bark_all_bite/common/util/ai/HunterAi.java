package com.infamous.all_bark_all_bite.common.util.ai;

import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
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

    public static Optional<LivingEntity> getStalkTarget(LivingEntity mob){
        return mob.getBrain().getMemory(ABABMemoryModuleTypes.STALK_TARGET.get());
    }

    public static Optional<LivingEntity> getPounceTarget(LivingEntity mob){
        return mob.getBrain().getMemory(ABABMemoryModuleTypes.POUNCE_TARGET.get());
    }

    public static void setStalkTarget(LivingEntity mob, LivingEntity target) {
        mob.getBrain().setMemory(ABABMemoryModuleTypes.STALK_TARGET.get(), target);
    }

    public static void stopStalking(LivingEntity mob) {
        mob.getBrain().eraseMemory(ABABMemoryModuleTypes.STALK_TARGET.get());
    }

    public static void setPounceTarget(LivingEntity mob, LivingEntity target) {
        mob.getBrain().setMemory(ABABMemoryModuleTypes.POUNCE_TARGET.get(), target);
    }

    public static void stopPouncing(LivingEntity mob) {
        mob.getBrain().eraseMemory(ABABMemoryModuleTypes.POUNCE_TARGET.get());
    }

    public static boolean isOnPounceCooldown(LivingEntity wolf){
        return wolf.getBrain().hasMemoryValue(ABABMemoryModuleTypes.POUNCE_COOLDOWN_TICKS.get());
    }

    public static void setPounceCooldown(LivingEntity mob, int cooldownTicks) {
        mob.getBrain().setMemory(ABABMemoryModuleTypes.POUNCE_COOLDOWN_TICKS.get(), cooldownTicks);
    }

    public static void clearPounceCooldown(LivingEntity wolf) {
        wolf.getBrain().eraseMemory(ABABMemoryModuleTypes.POUNCE_COOLDOWN_TICKS.get());
    }
}
