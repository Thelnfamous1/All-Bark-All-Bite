package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AngerAi;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.infamous.call_of_the_wild.common.util.HunterAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class StartHunting<E extends LivingEntity> extends Behavior<E> {
   public static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
   private static final int ANGER_TIME_IN_TICKS = 600;

   private final Predicate<E> canHuntPredicate;
   public StartHunting(Predicate<E> canHuntPredicate) {
      super(ImmutableMap.of(
              COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
              MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_ABSENT,
              MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.VALUE_ABSENT,
              COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(), MemoryStatus.REGISTERED,
              COTWMemoryModuleTypes.NEARBY_ADULTS.get(), MemoryStatus.REGISTERED));
      this.canHuntPredicate = canHuntPredicate;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canHuntPredicate.test(mob) && !HunterAi.hasAnyoneNearbyHuntedRecently(mob, GenericAi.getNearbyAdults(mob));
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      LivingEntity target = mob.getBrain().getMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get()).get();
      AngerAi.setAngerTarget(mob, target, ANGER_TIME_IN_TICKS);
      int huntCooldownInTicks = TIME_BETWEEN_HUNTS.sample(mob.level.random);
      HunterAi.setHuntedRecently(mob, huntCooldownInTicks);
      AngerAi.broadcastAngerTarget(GenericAi.getNearbyAdults(mob), target, ANGER_TIME_IN_TICKS);
      HunterAi.broadcastHuntedRecently(huntCooldownInTicks, GenericAi.getNearbyVisibleAdults(mob));
   }

}