package com.infamous.call_of_the_wild.common.behavior.sleep;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SleepOnGround<E extends LivingEntity> extends Behavior<E> {
    private static final long COOLDOWN_AFTER_BEING_WOKEN = 100L;
    private static final double MIN_Y_DIST_FROM_SLEEPING_POS = 0.0D;
    private static final double MAX_DIST_FROM_SLEEPING_POS = 1.14D;
    private final Predicate<E> canSleep;
    private final Consumer<E> onSleepTick;

    public SleepOnGround(Predicate<E> canSleep, Consumer<E> onSleepTick) {
        super(ImmutableMap.of(
                MemoryModuleType.LAST_WOKEN, MemoryStatus.REGISTERED
        ));
        this.canSleep = canSleep;
        this.onSleepTick = onSleepTick;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        if (entity.isPassenger()) {
            return false;
        } else {
            Optional<Long> lastWoken = entity.getBrain().getMemory(MemoryModuleType.LAST_WOKEN);
            if (lastWoken.isPresent()) {
                long ticksSinceLastWoken = level.getGameTime() - lastWoken.get();
                if (ticksSinceLastWoken > 0L && ticksSinceLastWoken < COOLDOWN_AFTER_BEING_WOKEN) {
                    return this.canSleep.test(entity);
                }
            }
            return this.canSleep.test(entity);
        }
    }

    @Override
    protected void start(ServerLevel level, E entity, long gameTime) {
        GenericAi.goToSleep(entity);
        entity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
        Optional<BlockPos> optionalSleepingPos = entity.getSleepingPos();
        if (optionalSleepingPos.isEmpty()) {
            return false;
        } else {
            BlockPos sleepingPos = optionalSleepingPos.get();
            return this.canSleep.test(entity)
                    && entity.getBrain().isActive(Activity.REST)
                    && entity.getY() > (double)sleepingPos.getY() + MIN_Y_DIST_FROM_SLEEPING_POS
                    && sleepingPos.closerToCenterThan(entity.position(), MAX_DIST_FROM_SLEEPING_POS);
        }
    }

    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void tick(ServerLevel level, E entity, long gameTime) {
        this.onSleepTick.accept(entity);
    }

    @Override
    protected void stop(ServerLevel level, E entity, long gameTime) {
        if(entity.isSleeping()){
            GenericAi.wakeUp(entity);
        }
    }
}
