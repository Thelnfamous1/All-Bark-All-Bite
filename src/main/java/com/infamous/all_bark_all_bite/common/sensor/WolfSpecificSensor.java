package com.infamous.all_bark_all_bite.common.sensor;

import com.google.common.collect.ImmutableSet;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfAi;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.Set;

public class WolfSpecificSensor extends Sensor<Wolf> {

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
                ABABMemoryModuleTypes.NEAREST_TARGETABLE_PLAYER_NOT_SNEAKING.get(),
                MemoryModuleType.NEAREST_ATTACKABLE);
    }

    @Override
    protected void doTick(ServerLevel level, Wolf wolf) {
        Brain<?> brain = wolf.getBrain();

        Optional<Player> nearestPlayerNotSneaking = Optional.empty();
        Optional<LivingEntity> nearestDisliked = Optional.empty();
        Optional<LivingEntity> nearestVisibleHuntable = Optional.empty();
        Optional<LivingEntity> nearestAttackable = Optional.empty();

        NearestVisibleLivingEntities nvle = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (LivingEntity livingEntity : nvle.findAll((le) -> true)) {
            if(nearestPlayerNotSneaking.isEmpty() && livingEntity instanceof Player player && isTargetablePlayerNotSneaking(wolf, player)){
                nearestPlayerNotSneaking = Optional.of(player);
            } else if(nearestDisliked.isEmpty() && WolfAi.isDisliked(livingEntity)){
                nearestDisliked = Optional.of(livingEntity);
            } else if(nearestVisibleHuntable.isEmpty() && isHuntable(wolf, livingEntity)){
                nearestVisibleHuntable = Optional.of(livingEntity);
            } else if(nearestAttackable.isEmpty() && isAttackable(wolf, livingEntity)){
                nearestAttackable = Optional.of(livingEntity);
            }
        }

        brain.setMemory(ABABMemoryModuleTypes.NEAREST_TARGETABLE_PLAYER_NOT_SNEAKING.get(), nearestPlayerNotSneaking);
        brain.setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), nearestDisliked);
        brain.setMemory(ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), nearestVisibleHuntable);
        brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, nearestAttackable);
    }

    public static boolean isTargetablePlayerNotSneaking(Wolf wolf, Player player) {
        return !WolfAi.isTrusting(wolf)
                && AiUtil.isAttackable(wolf, player, true)
                && AiUtil.isNotCreativeOrSpectator(player)
                && !player.isDiscrete();
    }

    private static boolean isAttackable(Wolf wolf, LivingEntity livingEntity) {
        return livingEntity.getType().is(ABABTags.WOLF_ALWAYS_HOSTILES) && AiUtil.isClose(wolf, livingEntity, ABABConfig.wolfTargetDetectionDistance.get())
                && AiUtil.isAttackable(wolf, livingEntity, true) && SharedWolfAi.wantsToAttack(wolf, livingEntity);
    }

    private static boolean isHuntable(Wolf wolf, LivingEntity livingEntity) {
        return livingEntity.getType().is(ABABTags.WOLF_HUNT_TARGETS) && AiUtil.isClose(wolf, livingEntity, ABABConfig.wolfTargetDetectionDistance.get())
                && AiUtil.isAttackable(wolf, livingEntity, true) && SharedWolfAi.wantsToAttack(wolf, livingEntity);
    }

}
