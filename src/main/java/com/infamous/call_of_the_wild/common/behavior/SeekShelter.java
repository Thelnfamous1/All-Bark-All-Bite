package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("NullableProblems")
public class SeekShelter extends Behavior<LivingEntity> {
   private final float speedModifier;

   public SeekShelter(float speedModifier) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
      this.speedModifier = speedModifier;
   }

   protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
      Optional<Vec3> shelterPosition = Optional.ofNullable(this.getShelterPosition(level, entity));
      if (shelterPosition.isPresent()) {
         entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, shelterPosition.map((vec3) -> new WalkTarget(vec3, this.speedModifier, 0)));
      }
   }

   protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
      return (level.isThundering() || level.isDay()) && level.canSeeSky(entity.blockPosition());
   }

   @Nullable
   private Vec3 getShelterPosition(ServerLevel level, LivingEntity entity) {
      RandomSource random = entity.getRandom();
      BlockPos blockPosition = entity.blockPosition();

      for(int i = 0; i < 10; ++i) {
         BlockPos offset = blockPosition.offset(
                 random.nextInt(20) - 10,
                 random.nextInt(6) - 3,
                 random.nextInt(20) - 10);
         if (!level.canSeeSky(offset)) {
            return Vec3.atBottomCenterOf(offset);
         }
      }
      return null;
   }

}