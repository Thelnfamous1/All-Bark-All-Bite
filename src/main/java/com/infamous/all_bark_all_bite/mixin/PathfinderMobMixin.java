package com.infamous.all_bark_all_bite.mixin;

import com.infamous.all_bark_all_bite.common.entity.LookTargetAccess;
import com.infamous.all_bark_all_bite.common.entity.WalkTargetAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PathfinderMob.class)
public abstract class PathfinderMobMixin extends Mob implements WalkTargetAccess, LookTargetAccess {
    private WalkTarget walkTarget;
    private PositionTracker lookTarget;

    protected PathfinderMobMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Nullable
    @Override
    public WalkTarget getWalkTarget() {
        return this.walkTarget;
    }

    @Override
    public void setWalkTarget(@Nullable WalkTarget walkTarget) {
        this.walkTarget = walkTarget;
    }

    @Nullable
    @Override
    public PositionTracker getLookTarget() {
        return this.lookTarget;
    }

    @Override
    public void setLookTarget(@Nullable PositionTracker lookTarget) {
        this.lookTarget = lookTarget;
    }
}
