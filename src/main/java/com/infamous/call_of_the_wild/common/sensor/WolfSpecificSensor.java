package com.infamous.call_of_the_wild.common.sensor;

import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.entity.wolf.WolfAi;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Wolf;

import java.util.Optional;
import java.util.Set;

public class WolfSpecificSensor extends Sensor<Wolf> {

    private static final int TARGET_DETECTION_DISTANCE = 16;

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
                MemoryModuleType.NEAREST_ATTACKABLE);
    }

    @Override
    protected void doTick(ServerLevel level, Wolf wolf) {
        Brain<?> brain = wolf.getBrain();

        Optional<LivingEntity> nearestDisliked = Optional.empty();
        Optional<LivingEntity> nearestVisibleHuntable = Optional.empty();
        Optional<LivingEntity> nearestAttackable = Optional.empty();

        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (LivingEntity livingEntity : nvle.findAll((le) -> true)) {
            if(nearestDisliked.isEmpty() && WolfAi.isDisliked(wolf, livingEntity)){
                nearestDisliked = Optional.of(livingEntity);
            } else if(nearestVisibleHuntable.isEmpty() && isHuntable(wolf, livingEntity)){
                nearestVisibleHuntable = Optional.of(livingEntity);
            } else if(nearestAttackable.isEmpty() && isAttackable(wolf, livingEntity)){
                nearestAttackable = Optional.of(livingEntity);
            }
        }

        brain.setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), nearestDisliked);
        brain.setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), nearestVisibleHuntable);
        brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestAttackable);
    }

    private static boolean isAttackable(Wolf wolf, LivingEntity livingEntity) {
        return livingEntity.getType().is(ABABTags.WOLF_ALWAYS_HOSTILES) && AiUtil.isClose(wolf, livingEntity, TARGET_DETECTION_DISTANCE)
                && AiUtil.isAttackable(wolf, livingEntity, true);
    }

    private static boolean isHuntable(Wolf wolf, LivingEntity livingEntity) {
        return livingEntity.getType().is(ABABTags.WOLF_HUNT_TARGETS) && AiUtil.isClose(wolf, livingEntity, TARGET_DETECTION_DISTANCE)
                && AiUtil.isAttackable(wolf, livingEntity, true);
    }

}
