package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.function.Predicate;

public class Sprint<E extends Mob> extends Behavior<E> {
    private final Predicate<E> canSprint;
    private final int tooFar;

    public Sprint(){
        this(e -> true, 0);
    }

    public Sprint(int tooFar){
        this(e -> true, tooFar);
    }

    public Sprint(Predicate<E> canSprint){
        this(canSprint, 0);
    }

    public Sprint(Predicate<E> canSprint, int tooFar) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.canSprint = canSprint;
        this.tooFar = tooFar;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        return this.canSprint.test(mob) && !GenericAi.getWalkTarget(mob).get().getTarget().currentPosition().closerThan(mob.position(), this.tooFar);
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        mob.setSprinting(true);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        return this.canSprint.test(mob) && !mob.getNavigation().isDone();
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(ServerLevel level, E mob, long gameTime) {
        mob.setSprinting(false);
    }
}
