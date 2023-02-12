package com.infamous.all_bark_all_bite.common.behavior.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public class StartItemActivityWithItemIfSeen<E extends LivingEntity> extends Behavior<E> {
   private final BiPredicate<E, ItemEntity> isWanted;
   private final MemoryModuleType<Boolean> itemActivity;

   public StartItemActivityWithItemIfSeen(BiPredicate<E, ItemEntity> isWanted, @NotNull MemoryModuleType<Boolean> itemActivity, @NotNull MemoryModuleType<Boolean> itemActivityDisabled, @NotNull MemoryModuleType<Boolean> disableWalkToActivityItem) {
      super(ImmutableMap.of(
              MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT,
              itemActivity, MemoryStatus.VALUE_ABSENT,
              itemActivityDisabled, MemoryStatus.VALUE_ABSENT,
              disableWalkToActivityItem, MemoryStatus.VALUE_ABSENT));
      this.isWanted = isWanted;
      this.itemActivity = itemActivity;
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      ItemEntity wantedItem = mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
      return this.isWanted.test(mob, wantedItem);
   }

   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      mob.getBrain().setMemory(this.itemActivity, true);
   }
}