package com.infamous.call_of_the_wild.common.behavior.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class StopHoldingItemIfNoLongerInItemActivity<E extends LivingEntity> extends Behavior<E> {
   private final Predicate<E> canStopHolding;
   private final Consumer<E> stopHolding;

   public StopHoldingItemIfNoLongerInItemActivity(Predicate<E> canStopHolding, Consumer<E> stopHolding, @NotNull MemoryModuleType<Boolean> itemActivity) {
      super(ImmutableMap.of(itemActivity, MemoryStatus.VALUE_ABSENT));
      this.canStopHolding = canStopHolding;
      this.stopHolding = stopHolding;
   }

   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canStopHolding.test(mob);
   }

   protected void start(ServerLevel level, E mob, long gameTime) {
      this.stopHolding.accept(mob);
   }
}