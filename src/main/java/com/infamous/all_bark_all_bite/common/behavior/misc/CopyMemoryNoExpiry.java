package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Predicate;

public class CopyMemoryNoExpiry<E extends LivingEntity, T> extends Behavior<E> {
   private final Predicate<E> predicate;
   private final MemoryModuleType<? extends T> sourceMemory;
   private final MemoryModuleType<T> targetMemory;

   public CopyMemoryNoExpiry(Predicate<E> predicate, MemoryModuleType<? extends T> sourceMemory, MemoryModuleType<T> targetMemory) {
      super(ImmutableMap.of(sourceMemory, MemoryStatus.VALUE_PRESENT, targetMemory, MemoryStatus.VALUE_ABSENT));
      this.predicate = predicate;
      this.sourceMemory = sourceMemory;
      this.targetMemory = targetMemory;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.predicate.test(mob);
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      Brain<?> brain = mob.getBrain();
      brain.setMemory(this.targetMemory, brain.getMemory(this.sourceMemory).get());
   }
}