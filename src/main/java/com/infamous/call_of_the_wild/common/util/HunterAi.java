package com.infamous.call_of_the_wild.common.util;

import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;
import java.util.Optional;

public class HunterAi {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasAnyoneNearbyHuntedRecently(LivingEntity mob, List<? extends LivingEntity> allies) {
       return mob.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY)
               || allies.stream().anyMatch((am) -> am.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY));
    }

    public static void setHuntedRecently(LivingEntity mob, int huntCooldownInTicks) {
       mob.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, huntCooldownInTicks);
    }

    public static void broadcastHuntedRecently(int huntCooldownInTicks, List<? extends LivingEntity> allies) {
       allies.forEach(am -> setHuntedRecently(am, huntCooldownInTicks));
    }

    public static Optional<LivingEntity> getNearestVisibleHuntable(LivingEntity mob){
        return mob.getBrain().getMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get());
    }

    public static Optional<LivingEntity> getStalkTarget(LivingEntity mob){
        return mob.getBrain().getMemory(COTWMemoryModuleTypes.STALK_TARGET.get());
    }

    public static void startHuntingUsingAngerTarget(LivingEntity hunter, int angerTimeInTicks, int huntCooldownInTicks) {
        getNearestVisibleHuntable(hunter).ifPresent(target -> {
            AngerAi.setAngerTarget(hunter, target, angerTimeInTicks);
            setHuntedRecently(hunter, huntCooldownInTicks);
            AngerAi.broadcastAngerTarget(GenericAi.getNearbyAdults(hunter), target, angerTimeInTicks);
            broadcastHuntedRecently(huntCooldownInTicks, GenericAi.getNearbyVisibleAdults(hunter));
        });
    }
}
