package com.infamous.all_bark_all_bite.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.ai.HunterAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.BiPredicate;

public class RememberIfHuntTargetWasKilled<E extends LivingEntity> extends Behavior<E> {

   private final BiPredicate<E, LivingEntity> huntTargetPredicate;
   private final UniformInt huntCooldown;

   public RememberIfHuntTargetWasKilled(BiPredicate<E, LivingEntity> huntTargetPredicate, UniformInt huntCooldown) {
      super(ImmutableMap.of(
              MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.REGISTERED));
      this.huntTargetPredicate = huntTargetPredicate;
      this.huntCooldown = huntCooldown;
   }

   protected void start(ServerLevel level, E mob, long gameTime) {
      if (this.isAttackTargetHuntTarget(mob)) {
         HunterAi.setHuntedRecently(mob, this.huntCooldown.sample(mob.getRandom()));
      }
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   private boolean isAttackTargetHuntTarget(E mob) {
      LivingEntity attackTarget = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
      return this.huntTargetPredicate.test(mob, attackTarget) && attackTarget.isDeadOrDying();
   }
}