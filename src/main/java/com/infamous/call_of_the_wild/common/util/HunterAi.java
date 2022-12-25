package com.infamous.call_of_the_wild.common.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

public class HunterAi {
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
}
