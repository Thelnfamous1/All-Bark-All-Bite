package com.infamous.all_bark_all_bite.common.sensor;

import com.google.common.collect.ImmutableSet;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AllySensor extends Sensor<LivingEntity> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(
              ABABMemoryModuleTypes.NEAREST_ALLIES.get(),
              ABABMemoryModuleTypes.NEAREST_ADULTS.get(),
              ABABMemoryModuleTypes.NEAREST_BABIES.get(),
              MemoryModuleType.NEAREST_LIVING_ENTITIES,
              ABABMemoryModuleTypes.NEAREST_VISIBLE_BABY.get(),
              ABABMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(),
              ABABMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
              ABABMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(),
              MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }

   protected void doTick(ServerLevel level, LivingEntity self) {
      self.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).ifPresent((nvle) -> this.setNearestVisibleAllies(self, nvle));
      self.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).ifPresent((nle) -> this.setNearestAllies(self, nle));
   }

   private void setNearestVisibleAllies(LivingEntity self, NearestVisibleLivingEntities nvle) {
      Optional<LivingEntity> nearestVisibleBaby = nvle.findClosest(le -> le.getType() == self.getType() && le.isBaby());
      self.getBrain().setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_BABY.get(), nearestVisibleBaby);

      List<LivingEntity> nearestVisibleAllies = new ArrayList<>();
      List<LivingEntity> nearestVisibleAdults = new ArrayList<>();
      List<LivingEntity> nearestVisibleBabies = new ArrayList<>();
      nvle.findAll(le -> le.getType() == self.getType())
              .forEach(le -> {
                 nearestVisibleAllies.add(le);
                 if(!le.isBaby()) nearestVisibleAdults.add(le);
                 else nearestVisibleBabies.add(le);
              });
      self.getBrain().setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(), nearestVisibleAllies);
      self.getBrain().setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(), nearestVisibleAdults);
      self.getBrain().setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(), nearestVisibleBabies);
   }

   private void setNearestAllies(LivingEntity self, List<LivingEntity> nle) {
      List<LivingEntity> nearestAllies = new ArrayList<>();
      List<LivingEntity> nearestAdults = new ArrayList<>();
      List<LivingEntity> nearestBabies = new ArrayList<>();

      nle.forEach(le -> {
         if(le.getType() == self.getType()){
            nearestAllies.add(le);
            if(!le.isBaby()) nearestAdults.add(le);
            else nearestBabies.add(le);
         }
      });

      self.getBrain().setMemory(ABABMemoryModuleTypes.NEAREST_ALLIES.get(), nearestAllies);
      self.getBrain().setMemory(ABABMemoryModuleTypes.NEAREST_ADULTS.get(), nearestAdults);
      self.getBrain().setMemory(ABABMemoryModuleTypes.NEAREST_BABIES.get(), nearestBabies);
   }
}