package com.infamous.all_bark_all_bite.common.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import org.jetbrains.annotations.Nullable;

public interface LookTargetAccessor {

    static LookTargetAccessor cast(PathfinderMob mob){
        return (LookTargetAccessor) mob;
    }

    @Nullable
    PositionTracker getLookTarget();

    void setLookTarget(@Nullable PositionTracker lookTarget);
}
