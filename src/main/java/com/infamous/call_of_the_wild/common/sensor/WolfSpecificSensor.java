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
    public static final double MAX_ALERTABLE_XZ = 12.0D;

    public static final double MAX_ALERTABLE_Y = 6.0D;
    private static final TargetingConditions ALERTABLE_CONDITIONS = TargetingConditions.forCombat().range(MAX_ALERTABLE_XZ).ignoreLineOfSight();

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
                MemoryModuleType.NEAREST_LIVING_ENTITIES,
                COTWMemoryModuleTypes.NEAREST_HUNTABLE.get(),
                COTWMemoryModuleTypes.IS_ALERT.get(),
                COTWMemoryModuleTypes.HAS_SHELTER.get());
    }

    @Override
    protected void doTick(ServerLevel level, Wolf wolf) {
        Brain<?> brain = wolf.getBrain();

        Optional<LivingEntity> nearestDisliked = Optional.empty();
        Optional<LivingEntity> nearestVisibleHuntable = Optional.empty();
        Optional<LivingEntity> nearestAttackable = Optional.empty();
        Optional<Player> nearestPlayerHoldingWantedItem = Optional.empty();

        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (LivingEntity livingEntity : nvle.findAll((le) -> true)) {
            if(nearestDisliked.isEmpty() && WolfAi.isDisliked(wolf, livingEntity)){
                nearestDisliked = Optional.of(livingEntity);
            } else if(nearestVisibleHuntable.isEmpty() && isHuntable(wolf, livingEntity, true)){
                nearestVisibleHuntable = Optional.of(livingEntity);
            } else if(nearestAttackable.isEmpty() && isAttackable(wolf, livingEntity, true)){
                nearestAttackable = Optional.of(livingEntity);
            }

            if (livingEntity instanceof Player player) {
                if (nearestPlayerHoldingWantedItem.isEmpty()
                        && !WolfGoalPackages.wantsToAvoidPlayer(wolf, player)
                        && player.isHolding(is -> WolfGoalPackages.isInteresting(wolf, is))) {
                    nearestPlayerHoldingWantedItem = Optional.of(player);
                }
            }
        }

        brain.setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), nearestDisliked);
        brain.setMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), nearestVisibleHuntable);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, nearestPlayerHoldingWantedItem);

        //

        Optional<LivingEntity> nearestHuntable = Optional.empty();
        Optional<Unit> alertable = Optional.empty();
        TargetingConditions alertableConditions = this.getAltertableConditions(wolf);

        List<LivingEntity> livingEntities = wolf.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of());

        for(LivingEntity livingEntity : livingEntities){
            if(nearestAttackable.isEmpty() && isAttackable(wolf, livingEntity, false)){
                nearestAttackable = Optional.of(livingEntity);
            }
            if(nearestHuntable.isEmpty() && isHuntable(wolf, livingEntity, false)){
                nearestHuntable = Optional.of(livingEntity);
            }

            if(alertable.isEmpty() && livingEntity.closerThan(wolf, MAX_ALERTABLE_XZ, MAX_ALERTABLE_Y) && alertableConditions.test(wolf, livingEntity)){
                alertable = Optional.of(Unit.INSTANCE);
            }
        }

        brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestAttackable);
        brain.setMemory(COTWMemoryModuleTypes.NEAREST_HUNTABLE.get(), nearestHuntable);
        brain.setMemory(COTWMemoryModuleTypes.IS_ALERT.get(), alertable);

        //

        Optional<Unit> hasShelter = this.hasShelter(level, wolf) ? Optional.of(Unit.INSTANCE) : Optional.empty();
        brain.setMemory(COTWMemoryModuleTypes.HAS_SHELTER.get(), hasShelter);
    }

    private TargetingConditions getAltertableConditions(Wolf wolf) {
        return ALERTABLE_CONDITIONS.copy().selector(le -> SharedWolfAi.canBeAlertedBy(wolf, le, l -> isHuntable(wolf, l, false)));
    }

    private static boolean isAttackable(Wolf wolf, LivingEntity livingEntity, boolean requireLineOfSight) {
        return AiUtil.isHostile(wolf, livingEntity, COTWTags.WOLF_ALWAYS_HOSTILES, requireLineOfSight);
    }

    private static boolean isHuntable(Wolf wolf, LivingEntity livingEntity, boolean requireLineOfSight) {
        return SharedWolfAi.isHuntable(wolf, livingEntity, COTWTags.WOLF_HUNT_TARGETS, requireLineOfSight);
    }

    private boolean hasShelter(ServerLevel level, Wolf wolf) {
        BlockPos topOfBodyPos = new BlockPos(wolf.getX(), wolf.getBoundingBox().maxY, wolf.getZ());
        return !level.canSeeSky(topOfBodyPos) && wolf.getWalkTargetValue(topOfBodyPos) >= 0.0F;
    }

}
