package com.infamous.call_of_the_wild.common.sensor;

import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.entity.dog.WolfAi;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class WolfSpecificSensor extends Sensor<Wolf> {
    private static final int LLAMA_MAX_STRENGTH = 5;

    private static final Predicate<Entity> AVOID_PLAYERS = (e) -> !e.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e);

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
    }

    @Override
    protected void doTick(ServerLevel level, Wolf wolf) {
        Brain<?> brain = wolf.getBrain();

        Optional<LivingEntity> nearestDisliked = Optional.empty();
        Optional<LivingEntity> nearestAttackable = Optional.empty();
        Optional<Player> nearestPlayerHoldingLovedItem = Optional.empty();

        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (LivingEntity livingEntity : nvle.findAll((le) -> true)) {
            if(nearestDisliked.isEmpty() && this.isDisliked(wolf, livingEntity)){
                nearestDisliked = Optional.of(livingEntity);
            } else if(nearestAttackable.isEmpty() && this.isAttackable(wolf, livingEntity)){
                nearestAttackable = Optional.of(livingEntity);
            } else if (livingEntity instanceof Player player) {
                if (nearestPlayerHoldingLovedItem.isEmpty()
                        && (player.isDiscrete() || wolf.isOwnedBy(player))
                        && !player.isSpectator()
                        && player.isHolding(is -> WolfAi.isInteresting(wolf, is))) {
                    nearestPlayerHoldingLovedItem = Optional.of(player);
                }
            }
        }

        brain.setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), nearestDisliked);
        brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestAttackable);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, nearestPlayerHoldingLovedItem);

    }

    private boolean isAttackable(Wolf wolf, LivingEntity target){
        return this.isClose(wolf, target) && (this.isHostileTarget(target) || this.isHuntTarget(wolf, target)) && Sensor.isEntityAttackable(wolf, target);
    }

    private boolean isHuntTarget(Wolf wolf, LivingEntity target) {
        return !wolf.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)
                && (target.getType().is(COTWTags.WOLF_HUNT_TARGETS)
                || target instanceof Turtle turtle && Turtle.BABY_ON_LAND_SELECTOR.test(turtle));
    }

    private boolean isHostileTarget(LivingEntity target) {
        return target.getType().is(COTWTags.WOLF_ALWAYS_HOSTILES);
    }

    private boolean isDisliked(Wolf wolf, LivingEntity target) {
        return (WolfAi.wantsToAvoid(target.getType())
                || target instanceof Player player && !wolf.isOwnedBy(player) && AVOID_PLAYERS.test(player)
                || target instanceof Llama llama && llama.getStrength() >= wolf.getRandom().nextInt(LLAMA_MAX_STRENGTH));
    }

    private boolean isClose(Wolf wolf, LivingEntity target) {
        double followRange = AiUtil.getFollowRange(wolf);
        return target.distanceToSqr(wolf) <= followRange * followRange;
    }

}
