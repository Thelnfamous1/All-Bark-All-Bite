package com.infamous.call_of_the_wild.common.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.entity.dog.WolfAi;
import com.infamous.call_of_the_wild.common.entity.dog.WolfGoalPackages;
import com.infamous.call_of_the_wild.common.entity.dog.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("NullableProblems")
public class WolfSpecificSensor extends Sensor<Wolf> {
    public static final double MAX_XZ_DIST = 12.0D;

    public static final double MAX_Y_DIST = 6.0D;
    private final TargetingConditions alertableTargeting = TargetingConditions.forCombat().range(MAX_XZ_DIST).ignoreLineOfSight();

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
                COTWMemoryModuleTypes.HAS_SHELTER.get(),
                COTWMemoryModuleTypes.ALERTABLE.get());
    }

    @Override
    protected void doTick(ServerLevel level, Wolf wolf) {
        Brain<?> brain = wolf.getBrain();

        Optional<LivingEntity> nearestDisliked = Optional.empty();
        Optional<LivingEntity> nearestHuntable = Optional.empty();
        Optional<LivingEntity> nearestAttackable = Optional.empty();
        Optional<Player> nearestPlayerHoldingWantedItem = Optional.empty();

        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (LivingEntity livingEntity : nvle.findAll((le) -> true)) {
            if(nearestDisliked.isEmpty() && WolfAi.isDisliked(wolf, livingEntity)){
                nearestDisliked = Optional.of(livingEntity);
            } else if(nearestHuntable.isEmpty() && isHuntable(wolf, livingEntity)){
                nearestHuntable = Optional.of(livingEntity);
            } else if(nearestAttackable.isEmpty() && isAttackable(wolf, livingEntity)){
                nearestAttackable = Optional.of(livingEntity);
            } else if (livingEntity instanceof Player player) {
                if (nearestPlayerHoldingWantedItem.isEmpty()
                        && !WolfGoalPackages.wantsToAvoidPlayer(wolf, player)
                        && player.isHolding(is -> WolfGoalPackages.isInteresting(wolf, is))) {
                    nearestPlayerHoldingWantedItem = Optional.of(player);
                }
            }
        }

        brain.setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), nearestDisliked);
        brain.setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), nearestHuntable);
        brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestAttackable);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, nearestPlayerHoldingWantedItem);


        List<LivingEntity> nearestMobs = wolf.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of());
        TargetingConditions copy = this.alertableTargeting.copy().selector(le -> SharedWolfAi.canBeAlertedBy(wolf, le, l -> isHuntable(wolf, l)));
        brain.eraseMemory(COTWMemoryModuleTypes.ALERTABLE.get());
        for(LivingEntity nearbyMob : nearestMobs){
            if(nearbyMob.closerThan(wolf, MAX_XZ_DIST, MAX_Y_DIST) && copy.test(wolf, nearbyMob)){
                brain.setMemory(COTWMemoryModuleTypes.ALERTABLE.get(), Unit.INSTANCE);
                break;
            }
        }

        brain.eraseMemory(COTWMemoryModuleTypes.HAS_SHELTER.get());
        if(hasShelter(level, wolf)) brain.setMemory(COTWMemoryModuleTypes.HAS_SHELTER.get(), Unit.INSTANCE);
    }

    private static boolean isAttackable(Wolf wolf, LivingEntity livingEntity) {
        return AiUtil.isAttackable(wolf, livingEntity, COTWTags.WOLF_ALWAYS_HOSTILES);
    }

    private static boolean isHuntable(Wolf wolf, LivingEntity livingEntity) {
        return SharedWolfAi.isHuntable(wolf, livingEntity, COTWTags.WOLF_HUNT_TARGETS);
    }

    protected boolean hasShelter(ServerLevel level, Wolf wolflike) {
        BlockPos topOfBodyPos = new BlockPos(wolflike.getX(), wolflike.getBoundingBox().maxY, wolflike.getZ());
        return !level.canSeeSky(topOfBodyPos) && wolflike.getWalkTargetValue(topOfBodyPos) >= 0.0F;
    }

}
