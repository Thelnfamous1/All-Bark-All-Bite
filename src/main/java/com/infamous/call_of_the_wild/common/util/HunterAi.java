package com.infamous.call_of_the_wild.common.util;

import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;
import java.util.Optional;

public class HunterAi {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasAnyoneNearbyHuntedRecently(LivingEntity mob, List<? extends LivingEntity> nearby) {
       return mob.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY)
               || nearby.stream().anyMatch((le) -> le.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY));
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

}
