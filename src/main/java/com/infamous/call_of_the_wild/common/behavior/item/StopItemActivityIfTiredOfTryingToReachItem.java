package com.infamous.call_of_the_wild.common.behavior.item;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("NullableProblems")
public class StopItemActivityIfTiredOfTryingToReachItem<E extends LivingEntity> extends Behavior<E> {
   private final int maxTimeToReachItem;
   private final int disableTime;
   private final Predicate<E> canGetTired;
   private final MemoryModuleType<Boolean> itemActivity;
   private final MemoryModuleType<Integer> timeTryingToReachActivityItem;
   private final MemoryModuleType<Boolean> disableWalkToActivityItem;

   public StopItemActivityIfTiredOfTryingToReachItem(Predicate<E> canGetTired, int maxTimeToReachItem, int disableTime, @NotNull MemoryModuleType<Boolean> itemActivity, @NotNull MemoryModuleType<Integer> timeTryingToReachActivityItem, @NotNull MemoryModuleType<Boolean> disableWalkToActivityItem) {
      super(ImmutableMap.of(
              itemActivity, MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT,
              timeTryingToReachActivityItem, MemoryStatus.REGISTERED,
              disableWalkToActivityItem, MemoryStatus.REGISTERED));
      this.canGetTired = canGetTired;
      this.maxTimeToReachItem = maxTimeToReachItem;
      this.disableTime = disableTime;
      this.itemActivity = itemActivity;
      this.timeTryingToReachActivityItem = timeTryingToReachActivityItem;
      this.disableWalkToActivityItem = disableWalkToActivityItem;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canGetTired.test(mob);
   }

   @Override
   protected void start(ServerLevel level, E mob, long p_35245_) {
      Brain<? extends LivingEntity> brain = mob.getBrain();
      Optional<Integer> timeTryingToReach = brain.getMemory(this.timeTryingToReachActivityItem);
      if (timeTryingToReach.isEmpty()) {
         brain.setMemory(this.timeTryingToReachActivityItem, 0);
      } else {
         int time = timeTryingToReach.get();
         if (time > this.maxTimeToReachItem) {
            brain.eraseMemory(this.itemActivity);
            brain.eraseMemory(this.timeTryingToReachActivityItem);
            brain.setMemoryWithExpiry(this.disableWalkToActivityItem, true, this.disableTime);
         } else {
            brain.setMemory(this.timeTryingToReachActivityItem, time + 1);
         }
      }

   }
}