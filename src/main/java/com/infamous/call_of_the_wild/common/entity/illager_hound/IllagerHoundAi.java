package com.infamous.call_of_the_wild.common.entity.illager_hound;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

public class IllagerHoundAi {
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
