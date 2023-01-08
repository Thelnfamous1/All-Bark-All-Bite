package com.infamous.call_of_the_wild.common.behavior.sleep;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class WakeUpTrigger<E extends LivingEntity> extends Behavior<E> {

    private final Predicate<E> wantsToWakeUp;

    public WakeUpTrigger(Predicate<E> wantsToWakeUp) {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.IS_SLEEPING.get(), MemoryStatus.REGISTERED
        ));
        this.wantsToWakeUp = wantsToWakeUp;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        return this.wantsToWakeUp.test(mob) && mob.isSleeping();
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        GenericAi.wakeUp(mob);
        mob.getBrain().eraseMemory(ABABMemoryModuleTypes.IS_SLEEPING.get());
    }
}
