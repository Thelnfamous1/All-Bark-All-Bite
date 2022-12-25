package com.infamous.call_of_the_wild.common.behavior.play;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.function.BiPredicate;

@SuppressWarnings("NullableProblems")
public class StartPlayingWithItemIfSeen<E extends LivingEntity> extends Behavior<E> {
   private final BiPredicate<E, ItemEntity> isWanted;

   public StartPlayingWithItemIfSeen(BiPredicate<E, ItemEntity> isWanted) {
      super(ImmutableMap.of(
              MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT,
              COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get(), MemoryStatus.VALUE_ABSENT,
              COTWMemoryModuleTypes.DISABLE_WALK_TO_PLAY_ITEM.get(), MemoryStatus.VALUE_ABSENT));
      this.isWanted = isWanted;
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      ItemEntity wantedItem = mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
      return this.isWanted.test(mob, wantedItem);
   }

   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      mob.getBrain().setMemory(COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get(), true);
   }
}