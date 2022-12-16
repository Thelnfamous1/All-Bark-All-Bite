package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;

import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

@SuppressWarnings("NullableProblems")
public class StopPlayingIfTiredOfTryingToReachItem<E extends LivingEntity> extends Behavior<E> {
   private final int maxTimeToReachItem;
   private final int disableTime;
   private final Predicate<E> canGetTired;

   public StopPlayingIfTiredOfTryingToReachItem(Predicate<E> canGetTired, int maxTimeToReachItem, int disableTime) {
      super(ImmutableMap.of(
              COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get(), MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT,
              COTWMemoryModuleTypes.TIME_TRYING_TO_REACH_PLAY_ITEM.get(), MemoryStatus.REGISTERED,
              COTWMemoryModuleTypes.DISABLE_WALK_TO_PLAY_ITEM.get(), MemoryStatus.REGISTERED));
      this.canGetTired = canGetTired;
      this.maxTimeToReachItem = maxTimeToReachItem;
      this.disableTime = disableTime;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canGetTired.test(mob);
   }

   @Override
   protected void start(ServerLevel level, E mob, long p_35245_) {
      Brain<? extends LivingEntity> brain = mob.getBrain();
      final MemoryModuleType<Integer> timeTryingToReachMemoryType = COTWMemoryModuleTypes.TIME_TRYING_TO_REACH_PLAY_ITEM.get();
      Optional<Integer> timeTryingToReach = brain.getMemory(timeTryingToReachMemoryType);
      if (timeTryingToReach.isEmpty()) {
         brain.setMemory(timeTryingToReachMemoryType, 0);
      } else {
         int time = timeTryingToReach.get();
         if (time > this.maxTimeToReachItem) {
            brain.eraseMemory(COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get());
            brain.eraseMemory(timeTryingToReachMemoryType);
            brain.setMemoryWithExpiry(COTWMemoryModuleTypes.DISABLE_WALK_TO_PLAY_ITEM.get(), true, this.disableTime);
         } else {
            brain.setMemory(timeTryingToReachMemoryType, time + 1);
         }
      }

   }
}