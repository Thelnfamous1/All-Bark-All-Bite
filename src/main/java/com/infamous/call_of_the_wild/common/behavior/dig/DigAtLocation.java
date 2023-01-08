package com.infamous.call_of_the_wild.common.behavior.dig;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.DigAi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.SoundType;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("NullableProblems")
public class DigAtLocation<E extends LivingEntity> extends Behavior<E> {
   private static final long CHECK_COOLDOWN = 40L;
   private long lastCheckTimestamp;
   private final Consumer<E> onDigCompleted;
   private final long digDuration;
   private long digUpAtTime;

   public DigAtLocation(Consumer<E> onDigCompleted, long digDuration) {
      super(ImmutableMap.of(
              ABABMemoryModuleTypes.DIG_LOCATION.get(), MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
      this.onDigCompleted = onDigCompleted;
      this.digDuration = digDuration;
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      if (this.lastCheckTimestamp != 0 && level.getGameTime() - this.lastCheckTimestamp < CHECK_COOLDOWN) {
         return false;
      } else {
         this.lastCheckTimestamp = level.getGameTime();
         GlobalPos digLocation = DigAi.getDigLocation(mob).get();
         return digLocation.dimension() == level.dimension() && digLocation.pos().closerToCenterThan(mob.position(), 1);
      }
   }

   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      this.lookAtDigLocation(mob);
      mob.setPose(Pose.DIGGING);
      this.digUpAtTime = gameTime + this.digDuration;
   }

   private void lookAtDigLocation(E mob) {
      DigAi.getDigLocation(mob).ifPresent((bp) -> mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(bp.pos())));
   }

   @Override
   protected void tick(ServerLevel level, E mob, long gameTime) {
      //this.lookAtDigLocation(mob);
      if(gameTime % 4L == 0L) DigAi.getDigLocation(mob).ifPresent((bp) -> this.playDiggingSound(level, mob, bp.pos()));
      if(gameTime >= this.digUpAtTime){
         this.onDigCompleted.accept(mob);
         mob.getBrain().eraseMemory(ABABMemoryModuleTypes.DIG_LOCATION.get());
      }
   }

   private void playDiggingSound(ServerLevel level, E mob, BlockPos bp) {
      SoundType soundType = level.getBlockState(bp.below()).getSoundType(level, bp, mob);
      mob.playSound(soundType.getHitSound());
   }

   @Override
   protected boolean timedOut(long gameTime) {
      return false;
   }

   @Override
   protected boolean canStillUse(ServerLevel level, E mob, long gameTime) {
      Optional<GlobalPos> optional = DigAi.getDigLocation(mob);
      if (optional.isEmpty()) {
         return false;
      } else {
         GlobalPos globalpos = optional.get();
         return globalpos.dimension() == level.dimension() && globalpos.pos().closerToCenterThan(mob.position(), 1);
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