package com.infamous.all_bark_all_bite.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.util.ai.HunterAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.Predicate;

public class StartHuntingPrey<E extends Mob> extends Behavior<E> {

   private final Predicate<E> canHuntPredicate;
   private final UniformInt huntCooldown;

   public StartHuntingPrey(Predicate<E> canHuntPredicate, UniformInt huntCooldown) {
      super(ImmutableMap.of(
              ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), MemoryStatus.VALUE_PRESENT,
              ABABMemoryModuleTypes.HUNT_TARGET.get(), MemoryStatus.VALUE_ABSENT,
              MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.VALUE_ABSENT,
              ABABMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(), MemoryStatus.REGISTERED,
              ABABMemoryModuleTypes.NEAREST_ADULTS.get(), MemoryStatus.REGISTERED)
      );
      this.canHuntPredicate = canHuntPredicate;
      this.huntCooldown = huntCooldown;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canHuntPredicate.test(mob) && !HunterAi.hasAnyoneNearbyHuntedRecently(mob, GenericAi.getNearbyVisibleAdults(mob));
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      LivingEntity target = this.getHuntable(mob).get();
      int huntCooldownInTicks = this.huntCooldown.sample(mob.level.random);
      HunterAi.setHuntedRecently(mob, huntCooldownInTicks);
      HunterAi.setHuntTarget(mob, target);
      HunterAi.broadcastHuntTarget(GenericAi.getNearbyAdults(mob), target);
      HunterAi.broadcastHuntedRecently(this.huntCooldown, GenericAi.getNearbyVisibleAdults(mob));
   }

   private Optional<LivingEntity> getHuntable(E mob) {
      return HunterAi.getNearestVisibleHuntable(mob);
   }
}