package com.infamous.call_of_the_wild.common.sensor;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import org.apache.commons.compress.utils.Lists;

@SuppressWarnings("NullableProblems")
public class AdultsSensor extends Sensor<AgeableMob> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(
              COTWMemoryModuleTypes.NEARBY_ADULTS.get(),
              MemoryModuleType.NEAREST_LIVING_ENTITIES,
              COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
              MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }

   protected void doTick(ServerLevel level, AgeableMob self) {
      self.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((nvle) -> this.setNearestVisibleAdults(self, nvle));
      self.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).ifPresent((nle) -> this.setNearbyAdults(self, nle));
   }

   private void setNearestVisibleAdults(AgeableMob self, NearestVisibleLivingEntities nvle) {
      List<AgeableMob> nearestVisibleAdults = Lists.newArrayList();

      nvle.findAll(le -> le.getType() == self.getType() && !le.isBaby())
              .forEach(le -> nearestVisibleAdults.add((AgeableMob) le));

      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(), nearestVisibleAdults);
   }

   private void setNearbyAdults(AgeableMob self, List<LivingEntity> nle) {
      List<AgeableMob> nearbyAdults = Lists.newArrayList();

      nle.forEach(le -> {
         if(le.getType() == self.getType() && !le.isBaby()) nearbyAdults.add((AgeableMob) le);
      });

      self.getBrain().setMemory(COTWMemoryModuleTypes.NEARBY_ADULTS.get(), nearbyAdults);
   }
}