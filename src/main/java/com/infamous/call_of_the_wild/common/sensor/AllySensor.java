package com.infamous.call_of_the_wild.common.sensor;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import org.apache.commons.compress.utils.Lists;

@SuppressWarnings("NullableProblems")
public class AllySensor extends Sensor<LivingEntity> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(
              COTWMemoryModuleTypes.NEAREST_ALLIES.get(),
              COTWMemoryModuleTypes.NEAREST_ADULTS.get(),
              COTWMemoryModuleTypes.NEAREST_BABIES.get(),
              MemoryModuleType.NEAREST_LIVING_ENTITIES,
              COTWMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(),
              COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
              COTWMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(),
              MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }

   protected void doTick(ServerLevel level, LivingEntity self) {
      self.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((nvle) -> this.setNearestVisibleAllies(self, nvle));
      self.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).ifPresent((nle) -> this.setNearestAllies(self, nle));
   }

   private void setNearestVisibleAllies(LivingEntity self, NearestVisibleLivingEntities nvle) {
      List<LivingEntity> nearestVisibleAllies = Lists.newArrayList();
      List<LivingEntity> nearestVisibleAdults = Lists.newArrayList();
      List<LivingEntity> nearestVisibleBabies = Lists.newArrayList();
      nvle.findAll(le -> le.getType() == self.getType())
              .forEach(le -> {
                 nearestVisibleAllies.add(le);
                 if(!le.isBaby()) nearestVisibleAdults.add(le);
                 else nearestVisibleBabies.add(le);
              });
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(), nearestVisibleAllies);
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(), nearestVisibleAdults);
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(), nearestVisibleBabies);
   }

   private void setNearestAllies(LivingEntity self, List<LivingEntity> nle) {
      List<LivingEntity> nearestAllies = Lists.newArrayList();
      List<LivingEntity> nearestAdults = Lists.newArrayList();
      List<LivingEntity> nearestBabies = Lists.newArrayList();

      nle.forEach(le -> {
         if(le.getType() == self.getType()){
            nearestAllies.add(le);
            if(!le.isBaby()) nearestAdults.add(le);
            else nearestBabies.add(le);
         }
      });

      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_ALLIES.get(), nearestAllies);
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_ADULTS.get(), nearestAdults);
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_BABIES.get(), nearestBabies);
   }
}