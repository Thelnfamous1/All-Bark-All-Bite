package com.infamous.call_of_the_wild.common.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.entity.wolf.WolfAi;
import com.infamous.call_of_the_wild.common.entity.wolf.WolfGoalPackages;
import com.infamous.call_of_the_wild.common.entity.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("NullableProblems")
public class WolfSpecificSensor extends Sensor<Wolf> {
    public static final int MAX_ALERTABLE_XZ = 12;

    public static final int MAX_ALERTABLE_Y = 6;
    private static final int TARGET_DETECTION_DISTANCE = 10;

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
                MemoryModuleType.NEAREST_ATTACKABLE,
                MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
                MemoryModuleType.NEAREST_LIVING_ENTITIES,
                ABABMemoryModuleTypes.IS_ALERT.get(),
                ABABMemoryModuleTypes.HAS_SHELTER.get());
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
            } else if(nearestVisibleHuntable.isEmpty() && isHuntable(wolf, livingEntity)){
                nearestVisibleHuntable = Optional.of(livingEntity);
            } else if(nearestAttackable.isEmpty() && isAttackable(wolf, livingEntity)){
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

        brain.setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), nearestDisliked);
        brain.setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), nearestVisibleHuntable);
        brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestAttackable);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, nearestPlayerHoldingWantedItem);

        Optional<Unit> alertable = Optional.empty();

        List<LivingEntity> livingEntities = wolf.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of());
        for(LivingEntity livingEntity : livingEntities){
            if(livingEntity.closerThan(wolf, MAX_ALERTABLE_XZ, MAX_ALERTABLE_Y)
                    && Sensor.isEntityAttackableIgnoringLineOfSight(wolf, livingEntity)
                    && SharedWolfAi.canBeAlertedBy(wolf, livingEntity)){
                alertable = Optional.of(Unit.INSTANCE);
                break;
            }
        }
        brain.setMemory(ABABMemoryModuleTypes.IS_ALERT.get(), alertable);

        brain.setMemory(ABABMemoryModuleTypes.HAS_SHELTER.get(), this.hasShelter(level, wolf) ? Optional.of(Unit.INSTANCE) : Optional.empty());
    }

    private static boolean isAttackable(Wolf wolf, LivingEntity livingEntity) {
        return AiUtil.isHostile(wolf, livingEntity, TARGET_DETECTION_DISTANCE, ABABTags.WOLF_ALWAYS_HOSTILES, true);
    }

    private static boolean isHuntable(Wolf wolf, LivingEntity livingEntity) {
        return SharedWolfAi.isHuntable(wolf, livingEntity, TARGET_DETECTION_DISTANCE, ABABTags.WOLF_HUNT_TARGETS, true);
    }

    private boolean hasShelter(ServerLevel level, Wolf wolf) {
        BlockPos topOfBodyPos = new BlockPos(wolf.getX(), wolf.getBoundingBox().maxY, wolf.getZ());
        return !level.canSeeSky(topOfBodyPos);
    }

}
