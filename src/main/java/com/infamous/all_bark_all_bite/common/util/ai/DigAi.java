package com.infamous.all_bark_all_bite.common.util.ai;

import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Predicate;

public class DigAi {

    public static void setDigLocation(LivingEntity livingEntity, BlockPos blockPos){
        livingEntity.getBrain().setMemory(ABABMemoryModuleTypes.DIG_LOCATION.get(), GlobalPos.of(livingEntity.level().dimension(), blockPos));
    }

    public static boolean hasDigLocation(LivingEntity livingEntity) {
        return livingEntity.getBrain().hasMemoryValue(ABABMemoryModuleTypes.DIG_LOCATION.get());
    }

    public static Optional<GlobalPos> getDigLocation(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(ABABMemoryModuleTypes.DIG_LOCATION.get());
    }

    public static Optional<BlockPos> generateDigLocation(PathfinderMob pathfinderMob, int maxXZ, int maxY, Predicate<BlockPos> digPosPredicate){
        Vec3 randomPos = GenericAi.getRandomNearbyPos(pathfinderMob, maxXZ, maxY);

        BlockPos blockPos = BlockPos.containing(randomPos);
        return Optional.of(blockPos).filter(digPosPredicate);
    }

    public static void eraseDigLocation(LivingEntity livingEntity) {
        livingEntity.getBrain().eraseMemory(ABABMemoryModuleTypes.DIG_LOCATION.get());
    }
}
