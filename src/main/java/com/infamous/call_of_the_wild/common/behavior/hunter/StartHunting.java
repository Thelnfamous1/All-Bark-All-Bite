package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import com.infamous.call_of_the_wild.common.ai.HunterAi;
import com.infamous.call_of_the_wild.common.entity.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.Predicate;

public class StartHunting<E extends Mob> extends Behavior<E> {

   private final Predicate<E> canHuntPredicate;
   private final UniformInt huntCooldown;

   public StartHunting(Predicate<E> canHuntPredicate, UniformInt huntCooldown) {
      super(ImmutableMap.of(
              ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
              MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.VALUE_ABSENT)
      );
      this.canHuntPredicate = canHuntPredicate;
      this.huntCooldown = huntCooldown;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      return this.canHuntPredicate.test(mob) && !HunterAi.hasAnyoneNearbyHuntedRecently(mob, GenericAi.getNearbyAdults(mob));
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      LivingEntity target = this.getHuntTarget(mob).get();
      int huntCooldownInTicks = this.huntCooldown.sample(mob.level.random);
      HunterAi.setHuntedRecently(mob, huntCooldownInTicks);
      StartAttacking.setAttackTarget(mob, target);
      GenericAi.broadcastAttackTarget(GenericAi.getNearbyAdults(mob), target);
      HunterAi.broadcastHuntedRecently(SharedWolfAi.TIME_BETWEEN_HUNTS, GenericAi.getNearbyVisibleAdults(mob));
   }

   private Optional<LivingEntity> getHuntTarget(E mob) {
      return HunterAi.getNearestVisibleHuntable(mob);
   }
}