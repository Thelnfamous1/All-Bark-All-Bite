package com.infamous.call_of_the_wild.common.sensor;

import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.infamous.call_of_the_wild.common.entity.dog.DogGoalPackages;
import com.infamous.call_of_the_wild.common.entity.dog.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.Set;

@SuppressWarnings("NullableProblems")
public class DogSpecificSensor extends Sensor<Dog> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    @Override
    protected void doTick(ServerLevel level, Dog dog) {
        boolean tame = dog.isTame();
        Brain<?> brain = dog.getBrain();

        Optional<LivingEntity> nearestDisliked = Optional.empty();
        Optional<LivingEntity> nearestHuntable = Optional.empty();
        Optional<LivingEntity> nearestAttackable = Optional.empty();
        Optional<Player> nearestPlayerHoldingLovedItem = Optional.empty();

        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (LivingEntity livingEntity : nvle.findAll((le) -> true)) {
            if(nearestDisliked.isEmpty()
                    && !tame
                    && SharedWolfAi.isDisliked(dog, livingEntity, COTWTags.DOG_DISLIKED)){
                nearestDisliked = Optional.of(livingEntity);
            } else if(nearestHuntable.isEmpty()
                    && !tame
                    && SharedWolfAi.isHuntable(dog, livingEntity, COTWTags.DOG_HUNT_TARGETS, true)){
                nearestHuntable = Optional.of(livingEntity);
            } else if(nearestAttackable.isEmpty()
                    && AiUtil.isHostile(dog, livingEntity, COTWTags.DOG_ALWAYS_HOSTILES, true)){
                nearestAttackable = Optional.of(livingEntity);
            } else if (livingEntity instanceof Player player) {
                if (nearestPlayerHoldingLovedItem.isEmpty()
                        && !player.isSpectator()
                        && player.isHolding(is -> DogGoalPackages.isInteresting(dog, is))) {
                    nearestPlayerHoldingLovedItem = Optional.of(player);
                }
            }
        }

        brain.setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), nearestDisliked);
        brain.setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), nearestHuntable);
        brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestAttackable);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, nearestPlayerHoldingLovedItem);
    }

}
