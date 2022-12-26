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
public class KinshipSensor extends Sensor<LivingEntity> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(
              COTWMemoryModuleTypes.NEARBY_KIN.get(),
              COTWMemoryModuleTypes.NEARBY_ADULTS.get(),
              COTWMemoryModuleTypes.NEARBY_BABIES.get(),
              MemoryModuleType.NEAREST_LIVING_ENTITIES,
              COTWMemoryModuleTypes.NEAREST_VISIBLE_KIN.get(),
              COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
              COTWMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(),
              MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }

   protected void doTick(ServerLevel level, LivingEntity self) {
      self.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((nvle) -> this.setNearestVisibleKin(self, nvle));
      self.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).ifPresent((nle) -> this.setNearbyKin(self, nle));
   }

   private void setNearestVisibleKin(LivingEntity self, NearestVisibleLivingEntities nvle) {
      List<LivingEntity> nearestVisibleKin = Lists.newArrayList();
      List<LivingEntity> nearestVisibleAdults = Lists.newArrayList();
      List<LivingEntity> nearestVisibleBabies = Lists.newArrayList();
      nvle.findAll(le -> le.getType() == self.getType())
              .forEach(le -> {
                 nearestVisibleKin.add(le);
                 if(!le.isBaby()) nearestVisibleAdults.add(le);
                 else nearestVisibleBabies.add(le);
              });
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_KIN.get(), nearestVisibleKin);
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(), nearestVisibleAdults);
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(), nearestVisibleBabies);
   }

   private void setNearbyKin(LivingEntity self, List<LivingEntity> nle) {
      List<LivingEntity> nearbyKin = Lists.newArrayList();
      List<LivingEntity> nearbyAdults = Lists.newArrayList();
      List<LivingEntity> nearbyBabies = Lists.newArrayList();

      nle.forEach(le -> {
         if(le.getType() == self.getType()){
            nearbyKin.add(le);
            if(!le.isBaby()) nearbyAdults.add(le);
            else nearbyBabies.add(le);
         }
      });

      self.getBrain().setMemory(COTWMemoryModuleTypes.NEARBY_KIN.get(), nearbyKin);
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEARBY_ADULTS.get(), nearbyAdults);
      self.getBrain().setMemory(COTWMemoryModuleTypes.NEARBY_BABIES.get(), nearbyBabies);
   }
}