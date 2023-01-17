package com.infamous.call_of_the_wild.common.behavior.sleep;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class StartSleeping extends Behavior<LivingEntity> {
    public static final long COOLDOWN_AFTER_BEING_WOKEN = 100L;

    public StartSleeping() {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.IS_SLEEPING.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.LAST_WOKEN, MemoryStatus.REGISTERED
        ));
    }

    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
        if (entity.isPassenger()) {
            return false;
        } else {
            Optional<Long> lastWoken = entity.getBrain().getMemory(MemoryModuleType.LAST_WOKEN);
            if (lastWoken.isPresent()) {
                long ticksSinceLastWoken = level.getGameTime() - lastWoken.get();
                return ticksSinceLastWoken <= 0L || ticksSinceLastWoken >= COOLDOWN_AFTER_BEING_WOKEN;
            }
            return true;
        }
    }

    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        GenericAi.goToSleep(entity);
        entity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        entity.getBrain().setMemory(ABABMemoryModuleTypes.IS_SLEEPING.get(), Unit.INSTANCE);
    }

}
