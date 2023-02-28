package com.infamous.all_bark_all_bite.common.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.jetbrains.annotations.Nullable;

public interface WalkTargetAccess {

    static WalkTargetAccess cast(PathfinderMob pathfinderMob){
        return (WalkTargetAccess) pathfinderMob;
    }

    @Nullable
    WalkTarget getWalkTarget();

    void setWalkTarget(@Nullable WalkTarget walkTarget);
}
