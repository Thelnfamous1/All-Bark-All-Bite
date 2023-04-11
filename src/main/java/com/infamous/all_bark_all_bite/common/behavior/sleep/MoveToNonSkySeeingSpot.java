package com.infamous.all_bark_all_bite.common.behavior.sleep;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class MoveToNonSkySeeingSpot{

   public static OneShot<LivingEntity> create(float speedModifier) {
      return BehaviorBuilder.create((builder) -> builder.group(builder.absent(MemoryModuleType.WALK_TARGET)).apply(builder, (wt) -> (level, entity, gameTime) -> {
         if (!level.canSeeSky(entity.blockPosition())) {
            return false;
         } else {
            Optional<Vec3> shelterPosition = Optional.ofNullable(getShelterPosition(level, entity));
            shelterPosition.ifPresent((sp) -> wt.set(new WalkTarget(sp, speedModifier, 0)));
            return true;
         }
      }));
   }

   @Nullable
   private static Vec3 getShelterPosition(ServerLevel level, LivingEntity entity) {
      RandomSource random = entity.getRandom();
      BlockPos blockPosition = entity.blockPosition();

      for(int i = 0; i < 10; ++i) {
         BlockPos offset = blockPosition.offset(
                 random.nextInt(20) - 10,
                 random.nextInt(6) - 3,
                 random.nextInt(20) - 10);
         if (hasBlocksAbove(level, entity, offset)) {
            return Vec3.atBottomCenterOf(offset);
         }
      }
      return null;
   }

   public static boolean hasBlocksAbove(Level level, LivingEntity entity, BlockPos blockPos) {
      return !level.canSeeSky(blockPos) && (double)level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > entity.getY();
   }

}