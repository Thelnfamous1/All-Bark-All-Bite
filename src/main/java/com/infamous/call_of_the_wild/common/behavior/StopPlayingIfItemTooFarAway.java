package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class StopPlayingIfItemTooFarAway<E extends LivingEntity> extends Behavior<E> {
   private final int maxDistanceToItem;
   private final Predicate<E> canStopPlaying;

   public StopPlayingIfItemTooFarAway(Predicate<E> canStopPlaying, int maxDistanceToItem) {
      super(ImmutableMap.of(
              COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get(), MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.REGISTERED));
      this.canStopPlaying = canStopPlaying;
      this.maxDistanceToItem = maxDistanceToItem;
   }

   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canStopPlaying.test(mob)
              && mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)
              .map(itemEntity -> !itemEntity.closerThan(mob, this.maxDistanceToItem))
              .orElse(true);
   }

   protected void start(ServerLevel level, E mob, long gameTime) {
      mob.getBrain().eraseMemory(COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get());
   }
}