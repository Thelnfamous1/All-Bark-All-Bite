package com.infamous.call_of_the_wild.common.ai;

import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Predicate;

public class DigAi {

    public static void setDigLocation(LivingEntity livingEntity, BlockPos blockPos){
        livingEntity.getBrain().setMemory(ABABMemoryModuleTypes.DIG_LOCATION.get(), GlobalPos.of(livingEntity.level.dimension(), blockPos));
    }

    public static Optional<GlobalPos> getDigLocation(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(ABABMemoryModuleTypes.DIG_LOCATION.get());
    }

    public static Optional<BlockPos> generateDigLocation(PathfinderMob pathfinderMob, int maxXZ, int maxY, Predicate<BlockPos> digPosPredicate){
        Vec3 randomPos = LandRandomPos.getPos(pathfinderMob, maxXZ, maxY);
        if(randomPos == null) return Optional.empty();

        BlockPos blockPos = new BlockPos(randomPos);
        return Optional.of(blockPos).filter(digPosPredicate);
    }
}
