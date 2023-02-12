package com.infamous.all_bark_all_bite.common.behavior.pet;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class StopSittingToWalk extends Behavior<TamableAnimal> {
    public StopSittingToWalk() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TamableAnimal tamableAnimal) {
        return tamableAnimal.isInSittingPose();
    }

    @Override
    protected void start(ServerLevel level, TamableAnimal tamableAnimal, long gameTime) {
        tamableAnimal.setInSittingPose(false);
    }
}
