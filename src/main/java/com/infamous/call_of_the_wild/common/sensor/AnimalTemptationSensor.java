package com.infamous.call_of_the_wild.common.sensor;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("NullableProblems")
public class AnimalTemptationSensor extends Sensor<Animal> {
   public static final int TEMPTATION_RANGE = 10;
   private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().range(TEMPTATION_RANGE).ignoreLineOfSight();

   @Override
   protected void doTick(ServerLevel level, Animal animal) {
      Brain<?> brain = animal.getBrain();
      List<ServerPlayer> temptingPlayers = level.players()
              .stream()
              .filter(EntitySelector.NO_SPECTATORS)
              .filter((sp) -> TEMPT_TARGETING.test(animal, sp))
              .filter((sp) -> animal.closerThan(sp, TEMPTATION_RANGE))
              .filter(sp -> this.playerHoldingFood(animal, sp))
              .sorted(Comparator.comparingDouble(animal::distanceToSqr))
              .toList();
      if (!temptingPlayers.isEmpty()) {
         Player player = temptingPlayers.get(0);
         brain.setMemory(MemoryModuleType.TEMPTING_PLAYER, player);
      } else {
         brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
      }
   }

   private boolean playerHoldingFood(Animal animal, Player player) {
      return player.isHolding(animal::isFood);
   }

   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
   }
}