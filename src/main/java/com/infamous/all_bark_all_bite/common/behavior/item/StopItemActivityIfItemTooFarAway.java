package com.infamous.all_bark_all_bite.common.behavior.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class StopItemActivityIfItemTooFarAway<E extends LivingEntity> extends Behavior<E> {
   private final int maxDistanceToItem;
   private final Predicate<E> canStopPlaying;
   private final MemoryModuleType<Boolean> itemActivity;

   public StopItemActivityIfItemTooFarAway(Predicate<E> canStopPlaying, int maxDistanceToItem, @NotNull MemoryModuleType<Boolean> itemActivity) {
      super(ImmutableMap.of(
              itemActivity, MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.REGISTERED));
      this.canStopPlaying = canStopPlaying;
      this.maxDistanceToItem = maxDistanceToItem;
      this.itemActivity = itemActivity;
   }

   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canStopPlaying.test(mob)
              && mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)
              .map(itemEntity -> !itemEntity.closerThan(mob, this.maxDistanceToItem))
              .orElse(true);
   }

   protected void start(ServerLevel level, E mob, long gameTime) {
      mob.getBrain().eraseMemory(this.itemActivity);
   }
}