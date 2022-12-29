package com.infamous.call_of_the_wild.common.behavior.wolflike;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;

@SuppressWarnings({"NullableProblems", "unused"})
public class Sleep<E extends LivingEntity> extends Behavior<E> {
    private final int cooldownAfterBeingWoken;
    private long nextOkStartTime;

    protected Sleep(int cooldownAfterBeingWoken) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.HAS_SHELTER.get(), MemoryStatus.VALUE_PRESENT,
                COTWMemoryModuleTypes.IS_ALERT.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LAST_SLEPT, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED,
                MemoryModuleType.LAST_WOKEN, MemoryStatus.REGISTERED
        ));
        this.cooldownAfterBeingWoken = cooldownAfterBeingWoken;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (mob.isPassenger()) {
            return false;
        } else {
            if (mob.xxa == 0.0F && mob.yya == 0.0F && mob.zza == 0.0F) {
                return this.canSleep(level, mob) || mob.isSleeping();
            } else {
                return false;
            }
        }
    }

    private boolean canSleep(ServerLevel level, E mob) {
        Optional<Long> lastWoken = mob.getBrain().getMemory(MemoryModuleType.LAST_WOKEN);
        if (lastWoken.isPresent()) {
            long timeDiff = level.getGameTime() - lastWoken.get();
            if (timeDiff > 0L && timeDiff < this.cooldownAfterBeingWoken) {
                return false;
            }
        }
        return level.isDay() 
                && mob.getBrain().hasMemoryValue(COTWMemoryModuleTypes.HAS_SHELTER.get()) 
                && !mob.getBrain().hasMemoryValue(COTWMemoryModuleTypes.IS_ALERT.get())
                && !mob.isInPowderSnow;
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        if (gameTime > this.nextOkStartTime) {
            GenericAi.startSleeping(mob, mob.blockPosition());
        }
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    public boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        Optional<BlockPos> sleepingPos = mob.getSleepingPos();
        if (sleepingPos.isEmpty()) {
            return false;
        } else {
            return mob.getBrain().isActive(Activity.REST)
                    && sleepingPos.get().closerToCenterThan(mob.position(), 1.14D)
                    && this.canSleep(level, mob);
        }
    }

    @Override
    public void stop(ServerLevel level, E mob, long gameTime) {
        if (mob.isSleeping()) {
            GenericAi.stopSleeping(mob);
            this.nextOkStartTime = gameTime + 40L;
        }
    }
}
