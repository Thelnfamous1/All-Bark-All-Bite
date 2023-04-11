package com.infamous.all_bark_all_bite.common.behavior.misc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.function.Predicate;

public class CopyMemoryNoExpiry{

   public static <E extends LivingEntity, T> BehaviorControl<E> create(Predicate<E> copyIf, MemoryModuleType<? extends T> from, MemoryModuleType<T> to) {
      return BehaviorBuilder.create((builder) -> builder.group(builder.present(from), builder.absent(to)).apply(builder, (f, t) -> (level, entity, gameTime) -> {
         if (!copyIf.test(entity)) {
            return false;
         } else {
            t.set(builder.get(f));
            return true;
         }
      }));
   }
}