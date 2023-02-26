package com.infamous.all_bark_all_bite.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

public class VectorTracker implements PositionTracker {
    private final BlockPos blockPos;
    private final Vec3 position;

    public VectorTracker(Vec3 position) {
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

    @Override
    public String toString() {
        return "VectorTracker{position=" + this.position + ", blockPos=" + this.blockPos + "}";
    }
}
