package com.infamous.all_bark_all_bite.common.entity.illager_hound;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABSensorTypes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class IllagerHoundAi {
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super IllagerHound>>> SENSOR_TYPES =
            ImmutableList.of(
                    SensorType.HURT_BY,
                    SensorType.NEAREST_LIVING_ENTITIES,
                    ABABSensorTypes.NEAREST_ALLIES.get(),
                    SensorType.NEAREST_PLAYERS
            );
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES =
            ImmutableList.of(
                    MemoryModuleType.ATTACK_TARGET,
                    MemoryModuleType.ATTACK_COOLING_DOWN,
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                    MemoryModuleType.HURT_BY,
                    MemoryModuleType.HURT_BY_ENTITY,
                    ABABMemoryModuleTypes.IS_FOLLOWING.get(),
                    MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
                    MemoryModuleType.LONG_JUMP_MID_JUMP,
                    MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.NEAREST_LIVING_ENTITIES,
                    ABABMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(),
                    MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                    MemoryModuleType.NEAREST_VISIBLE_PLAYER,
                    MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
                    MemoryModuleType.PATH,
                    MemoryModuleType.WALK_TARGET
            );

    public static void makeBrain(Brain<IllagerHound> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
    }

    private static void initCoreActivity(Brain<IllagerHound> brain) {
        brain.addActivityWithConditions(Activity.CORE, IllagerHoundGoalPackages.getCorePackage(), ImmutableSet.of());
    }

    private static void initIdleActivity(Brain<IllagerHound> brain) {
        brain.addActivityWithConditions(Activity.IDLE, IllagerHoundGoalPackages.getIdlePackage(), ImmutableSet.of());
    }

    private static void initFightActivity(Brain<IllagerHound> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.FIGHT, IllagerHoundGoalPackages.getFightPackage(),
                ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)),
                ImmutableSet.of(MemoryModuleType.ATTACK_TARGET));
    }

    protected static void updateActivity(IllagerHound illagerHound) {
        Activity prev = illagerHound.getBrain().getActiveNonCoreActivity().orElse(null);
        illagerHound.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        Activity current = illagerHound.getBrain().getActiveNonCoreActivity().orElse(null);
        if (current == Activity.FIGHT && prev != Activity.FIGHT) {
            illagerHound.playAngrySound();
        }

        illagerHound.setAggressive(illagerHound.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        illagerHound.setSprinting(illagerHound.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
    }
}
