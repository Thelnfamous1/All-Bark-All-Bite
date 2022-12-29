package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class StartHunting<E extends LivingEntity> extends Behavior<E> {

   private final Predicate<E> canHuntPredicate;
   private final Consumer<E> startHunting;

   public StartHunting(Predicate<E> canHuntPredicate, Consumer<E> startHunting) {
      super(ImmutableMap.of(
              COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
              MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.VALUE_ABSENT)
      );
      this.canHuntPredicate = canHuntPredicate;
      this.startHunting = startHunting;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canHuntPredicate.test(mob);
   }

   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      this.startHunting.accept(mob);
   }
}