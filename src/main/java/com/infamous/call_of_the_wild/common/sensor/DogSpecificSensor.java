package com.infamous.call_of_the_wild.common.sensor;

import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.behavior.Beg;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.infamous.call_of_the_wild.common.entity.dog.DogAi;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.Set;

public class DogSpecificSensor extends Sensor<Dog> {
    private static final int LLAMA_MAX_STRENGTH = 5;
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    @Override
    protected void doTick(ServerLevel level, Dog dog) {
        Brain<?> brain = dog.getBrain();

        Optional<LivingEntity> nearestDisliked = Optional.empty();
        Optional<LivingEntity> nearestAttackable = Optional.empty();
        Optional<Player> nearestPlayerHoldingLovedItem = Optional.empty();

        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (LivingEntity livingEntity : nvle.findAll((le) -> true)) {
            if(nearestDisliked.isEmpty() && this.isDisliked(dog, livingEntity)){
                nearestDisliked = Optional.of(livingEntity);
            } else if(nearestAttackable.isEmpty() && this.isAttackable(dog, livingEntity)){
                nearestAttackable = Optional.of(livingEntity);
            } else if (livingEntity instanceof Player player) {
                if (nearestPlayerHoldingLovedItem.isEmpty() && !player.isSpectator() && Beg.playerHoldingInteresting(player, dog)) {
                    nearestPlayerHoldingLovedItem = Optional.of(player);
                }
            }
        }

        brain.setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), nearestDisliked);
        brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestAttackable);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, nearestPlayerHoldingLovedItem);
    }

    private boolean isAttackable(Dog dog, LivingEntity target){
        return this.isClose(dog, target) && (this.isHostileTarget(target) || this.isHuntTarget(dog, target)) && Sensor.isEntityAttackable(dog, target);
    }

    private boolean isHuntTarget(Dog dog, LivingEntity target) {
        return !dog.isTame()
                && !dog.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)
                && (target.getType().is(COTWTags.DOG_HUNT_TARGETS)
                    || target instanceof Turtle turtle && Turtle.BABY_ON_LAND_SELECTOR.test(turtle));
    }

    private boolean isHostileTarget(LivingEntity target) {
        return target.getType().is(COTWTags.DOG_ALWAYS_HOSTILES);
    }

    private boolean isDisliked(Dog dog, LivingEntity target) {
        return !dog.isTame()
                && (DogAi.wantsToAvoid(target.getType())
                    || target instanceof Llama llama && llama.getStrength() >= dog.getRandom().nextInt(LLAMA_MAX_STRENGTH));
    }

    private boolean isClose(Dog dog, LivingEntity target) {
        double followRange = AiHelper.getFollowRange(dog);
        return target.distanceToSqr(dog) <= followRange * followRange;
    }

}
