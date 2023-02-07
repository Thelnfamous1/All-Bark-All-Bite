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
import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class StartSleeping<E extends LivingEntity> extends Behavior<E> {
    public static final long COOLDOWN_AFTER_BEING_WOKEN = 100L;
    private final Predicate<E> canSleep;

    public StartSleeping(Predicate<E> canSleep) {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.IS_SLEEPING.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.LAST_WOKEN, MemoryStatus.REGISTERED
        ));
        this.canSleep = canSleep;
    }

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

    protected void start(ServerLevel level, E entity, long gameTime) {
        GenericAi.goToSleep(entity);
        entity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        entity.getBrain().setMemory(ABABMemoryModuleTypes.IS_SLEEPING.get(), Unit.INSTANCE);
    }

}
