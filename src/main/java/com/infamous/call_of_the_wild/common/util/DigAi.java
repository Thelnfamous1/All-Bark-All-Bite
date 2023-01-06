package com.infamous.call_of_the_wild.common.util;

import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class DigAi {

    public static void setDigLocation(LivingEntity livingEntity, BlockPos blockPos){
        livingEntity.getBrain().setMemory(ABABMemoryModuleTypes.DIG_LOCATION.get(), GlobalPos.of(livingEntity.level.dimension(), blockPos));
    }

    public static Optional<GlobalPos> getDigLocation(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(ABABMemoryModuleTypes.DIG_LOCATION.get());
    }
}
