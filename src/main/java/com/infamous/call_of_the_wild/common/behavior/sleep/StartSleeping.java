package com.infamous.call_of_the_wild.common.behavior.sleep;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings({"NullableProblems", "unused"})
public class StartSleeping<E extends LivingEntity> extends Behavior<E> {
    private final Predicate<E> canSleep;

    public StartSleeping(Predicate<E> canSleep) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.IS_SLEEPING.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LAST_WOKEN, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED
        ));
        this.canSleep = canSleep;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (mob.isPassenger()) {
            return false;
        } else {
            if (mob.xxa == 0.0F && mob.yya == 0.0F && mob.zza == 0.0F) {
                return this.isNotOnSleepCooldown(mob) && this.canSleep.test(mob);
            } else {
                return false;
            }
        }
    }

    private boolean isNotOnSleepCooldown(E mob){
        Optional<Long> lastWoken = mob.getBrain().getMemory(MemoryModuleType.LAST_WOKEN);
        if (lastWoken.isPresent()) {
            long timeDiff = mob.level.getGameTime() - lastWoken.get();
            return timeDiff <= 0L || timeDiff >= 100L;
        }
        return true;
    }

    @Override
    public void start(ServerLevel level, E mob, long gameTime) {
        mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        GenericAi.goToSleep(mob);
        mob.getBrain().setMemory(COTWMemoryModuleTypes.IS_SLEEPING.get(), Unit.INSTANCE);
    }

}
