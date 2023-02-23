package com.infamous.all_bark_all_bite.common.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.jetbrains.annotations.Nullable;

public interface WalkTargetAccessor {

    static WalkTargetAccessor cast(PathfinderMob pathfinderMob){
        return (WalkTargetAccessor) pathfinderMob;
    }

    @Nullable
    WalkTarget getWalkTarget();

    void setWalkTarget(@Nullable WalkTarget walkTarget);
}
