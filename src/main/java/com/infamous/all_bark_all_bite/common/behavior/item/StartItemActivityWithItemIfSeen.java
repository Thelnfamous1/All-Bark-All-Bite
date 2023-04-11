package com.infamous.all_bark_all_bite.common.behavior.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public class StartItemActivityWithItemIfSeen{
   public static <E extends LivingEntity> OneShot<E> create(BiPredicate<E, ItemEntity> isWanted, @NotNull MemoryModuleType<Boolean> itemActivity, @NotNull MemoryModuleType<Boolean> itemActivityDisabled, @NotNull MemoryModuleType<Boolean> disableWalkToActivityItem) {
      return BehaviorBuilder.create((builder) -> builder.group(builder.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), builder.absent(itemActivity), builder.absent(itemActivityDisabled), builder.absent(disableWalkToActivityItem)).apply(builder, (nvwi, ia, iad, dwtai) -> (level, entity, gameTime) -> {
         ItemEntity wantedItem = builder.get(nvwi);
         if (!isWanted.test(entity, wantedItem)) {
            return false;
         } else {
            ia.set(true);
            return true;
         }
      }));
   }
}