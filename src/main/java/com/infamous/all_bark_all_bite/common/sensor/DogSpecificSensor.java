package com.infamous.all_bark_all_bite.common.sensor;

import com.google.common.collect.ImmutableSet;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.common.entity.dog.DogAi;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.Set;

public class DogSpecificSensor extends Sensor<Dog> {

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
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
        Optional<Player> nearestPlayerHoldingWantedItem = Optional.empty();

        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (LivingEntity livingEntity : nvle.findAll((le) -> true)) {
            if(nearestDisliked.isEmpty()
                    && !tame
                    && DogAi.isDisliked(livingEntity)){
                nearestDisliked = Optional.of(livingEntity);
            } else if(nearestHuntable.isEmpty()
                    && !tame
                    && isHuntable(dog, livingEntity)){
                nearestHuntable = Optional.of(livingEntity);
            } else if(nearestAttackable.isEmpty()
                    && isAttackable(dog, livingEntity)){
                nearestAttackable = Optional.of(livingEntity);
            } else if (livingEntity instanceof Player player) {
                if (nearestPlayerHoldingWantedItem.isEmpty()
                        && !player.isSpectator()
                        && player.isHolding(is -> DogAi.isInteresting(dog, is))) {
                    nearestPlayerHoldingWantedItem = Optional.of(player);
                }
            }
        }

        brain.setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), nearestDisliked);
        brain.setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), nearestHuntable);
        brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestAttackable);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, nearestPlayerHoldingWantedItem);
    }

    private static boolean isAttackable(Dog dog, LivingEntity livingEntity) {
        return livingEntity.getType().is(ABABTags.DOG_ALWAYS_HOSTILES) && AiUtil.isClose(dog, livingEntity, ABABConfig.dogTargetDetectionDistance.get())
                && AiUtil.isAttackable(dog, livingEntity, true) && SharedWolfAi.wantsToAttack(dog, livingEntity);
    }

    private static boolean isHuntable(Dog dog, LivingEntity livingEntity) {
        return livingEntity.getType().is(ABABTags.DOG_HUNT_TARGETS) && AiUtil.isClose(dog, livingEntity, ABABConfig.dogTargetDetectionDistance.get())
                && AiUtil.isAttackable(dog, livingEntity, true) && SharedWolfAi.wantsToAttack(dog, livingEntity);
    }

}
