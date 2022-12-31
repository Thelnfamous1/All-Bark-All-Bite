package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

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

    @Override
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
        Optional<Vec3> hidePos = this.getHidePos(level, mob);
        if (hidePos.isEmpty()) {
            return false;
        } else {
            this.wantedPos = hidePos.get();
            return true;
        }
    }

    protected Optional<Vec3> getHidePos(ServerLevel level, E mob) {
        return BlockPos.findClosestMatch(
                mob.blockPosition(),
                10,
                3,
                bp -> !level.canSeeSky(bp) && mob.getWalkTargetValue(bp) < 0.0F)
                .map(Vec3::atBottomCenterOf);
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.wantedPos, this.speedModifier, 2));
    }
}
