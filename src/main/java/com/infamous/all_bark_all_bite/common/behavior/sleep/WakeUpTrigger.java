package com.infamous.all_bark_all_bite.common.behavior.sleep;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.function.Predicate;

public class WakeUpTrigger<E extends LivingEntity> extends Behavior<E> {

    private final Predicate<E> wantsToWakeUp;

    public WakeUpTrigger(Predicate<E> wantsToWakeUp) {
        super(ImmutableMap.of());
        this.wantsToWakeUp = wantsToWakeUp;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        return this.wantsToWakeUp.test(mob) && mob.isSleeping();
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        GenericAi.wakeUp(mob);
    }
}
