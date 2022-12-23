package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.SoundType;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("NullableProblems")
public class DigAtLocation<E extends LivingEntity> extends Behavior<E> {
   private static final long CHECK_COOLDOWN = 10L;
   private static final double DISTANCE = 1.73D;
   private long lastCheck;
   private final Consumer<E> onDigCompleted;
   private final long digDuration;
   private long digUpAtTime;

   public DigAtLocation(Consumer<E> onDigCompleted, long digDuration) {
      super(ImmutableMap.of(
              COTWMemoryModuleTypes.DIG_LOCATION.get(), MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
      this.onDigCompleted = onDigCompleted;
      this.digDuration = digDuration;
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      if (level.getGameTime() - this.lastCheck < CHECK_COOLDOWN) {
         return false;
      } else {
         this.lastCheck = level.getGameTime();
         BlockPos blockPos = mob.getBrain().getMemory(COTWMemoryModuleTypes.DIG_LOCATION.get()).get();
         return blockPos.closerToCenterThan(mob.position(), DISTANCE);
      }
   }

   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      Brain<?> brain = mob.getBrain();
      brain.getMemory(COTWMemoryModuleTypes.DIG_LOCATION.get()).ifPresent((bp) -> brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(bp)));
      mob.setPose(Pose.DIGGING);
      this.digUpAtTime = gameTime + this.digDuration;
   }

   @Override
   protected void tick(ServerLevel level, E mob, long gameTime) {
      Brain<?> brain = mob.getBrain();
      if(gameTime % 4L == 0L)
         brain.getMemory(COTWMemoryModuleTypes.DIG_LOCATION.get())
              .ifPresent((bp) -> {
                 SoundType soundType = level.getBlockState(bp.below()).getSoundType(level, bp, mob);
                 mob.playSound(soundType.getHitSound());
              });
      if(gameTime >= this.digUpAtTime){
         this.onDigCompleted.accept(mob);
         brain.eraseMemory(COTWMemoryModuleTypes.DIG_LOCATION.get());
      }
   }

   @Override
   protected boolean timedOut(long gameTime) {
      return false;
   }

   @Override
   protected boolean canStillUse(ServerLevel level, E mob, long gameTime) {
      Optional<BlockPos> optional = mob.getBrain().getMemory(COTWMemoryModuleTypes.DIG_LOCATION.get());
      if (optional.isEmpty()) {
         return false;
      } else {
         BlockPos blockPos = optional.get();
         return blockPos.closerToCenterThan(mob.position(), DISTANCE);
      }
   }

   @Override
   protected void stop(ServerLevel level, E mob, long gameTime) {
      this.digUpAtTime = 0L;
      if (mob.hasPose(Pose.DIGGING)) {
         mob.setPose(Pose.STANDING);
      }
   }
}