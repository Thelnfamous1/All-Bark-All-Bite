package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

@SuppressWarnings("NullableProblems")
public class FleeSun<E extends PathfinderMob> extends Behavior<E> {
    private Vec3 wantedPos;
    private final float speedModifier;

    public FleeSun(float speedModifier) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED
        ));
        this.speedModifier = speedModifier;
    }

    public boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (!level.isDay()) {
            return false;
        } else if (!mob.isOnFire()) {
            return false;
        } else if (!level.canSeeSky(mob.blockPosition())) {
            return false;
        } else {
            return mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && this.setWantedPos(level, mob);
        }
    }

    protected boolean setWantedPos(ServerLevel level, E mob) {
        Vec3 hidePos = this.getHidePos(level, mob);
        if (hidePos == null) {
            return false;
        } else {
            this.wantedPos = hidePos;
            return true;
        }
    }

    public boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        return !mob.getNavigation().isDone();
    }

    public void start(ServerLevel level, E mob, long gameTime) {
        mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.wantedPos, this.speedModifier, 2));
    }

    @Nullable
    protected Vec3 getHidePos(ServerLevel level, E mob) {
        RandomSource random = mob.getRandom();
        BlockPos blockPos = mob.blockPosition();

        for(int i = 0; i < 10; ++i) {
            BlockPos maybeHidePos = blockPos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (!level.canSeeSky(maybeHidePos) && mob.getWalkTargetValue(maybeHidePos) < 0.0F) {
                return Vec3.atBottomCenterOf(maybeHidePos);
            }
        }

        return null;
    }
}
