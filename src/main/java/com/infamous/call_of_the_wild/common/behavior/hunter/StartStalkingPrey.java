package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.ai.HunterAi;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class StartStalkingPrey<E extends Mob> extends Behavior<E> {
   private final Predicate<E> canStalk;

   private final Function<E, Optional<? extends LivingEntity>> targetFinder;

   public StartStalkingPrey(Predicate<E> canStalk, Function<E, Optional<? extends LivingEntity>> targetFinder) {
      super(ImmutableMap.of(
              ABABMemoryModuleTypes.STALK_TARGET.get(), MemoryStatus.VALUE_ABSENT,
              ABABMemoryModuleTypes.POUNCE_TARGET.get(), MemoryStatus.VALUE_ABSENT,
              ABABMemoryModuleTypes.POUNCE_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT)
      );
      this.canStalk = canStalk;
      this.targetFinder = targetFinder;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canStalk.test(mob) && this.targetFinder.apply(mob).isPresent();
   }

   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      this.targetFinder.apply(mob).ifPresent(
              target -> HunterAi.setStalkTarget(mob, target)
      );
   }
}