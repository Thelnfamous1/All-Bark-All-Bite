package com.infamous.call_of_the_wild.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("NullableProblems")
public class SimplePositionTracker implements PositionTracker {
    private final BlockPos blockPos;
    private final Vec3 position;

    public SimplePositionTracker(Vec3 position) {
        this.blockPos = new BlockPos(position).immutable();
        this.position = position;
    }

    @Override
    public Vec3 currentPosition() {
        return this.position;
    }

    @Override
    public BlockPos currentBlockPosition() {
        return this.blockPos;
    }

    @Override
    public boolean isVisibleBy(LivingEntity livingEntity) {
        return true;
    }

    public String toString() {
        return "SimplePositionTracker{position=" + this.position + ", blockPos=" + this.blockPos + "}";
    }
}
