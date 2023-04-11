package com.infamous.all_bark_all_bite.common.behavior.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;

import java.util.function.Predicate;

public class RunBehaviorIf<E extends LivingEntity> implements BehaviorControl<E> {

    private final Predicate<E> predicate;
    private final BehaviorControl<? super E> wrappedBehavior;

    public RunBehaviorIf(Predicate<E> predicate, BehaviorControl<? super E> wrappedBehavior) {
        this.predicate = predicate;
        this.wrappedBehavior = wrappedBehavior;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.wrappedBehavior.getStatus();
    }

    @Override
    public boolean tryStart(ServerLevel level, E mob, long gameTime) {
        return this.predicate.test(mob) && this.wrappedBehavior.tryStart(level, mob, gameTime);
    }

    @Override
    public void tickOrStop(ServerLevel level, E mob, long gameTime) {
        if(this.predicate.test(mob)){
            this.wrappedBehavior.tickOrStop(level, mob, gameTime);
        } else{
            this.stopIfRunning(level, mob, gameTime);
        }
    }

    @Override
    public void doStop(ServerLevel level, E mob, long gameTime) {
        this.stopIfRunning(level, mob, gameTime);
    }

    private void stopIfRunning(ServerLevel level, E mob, long gameTime) {
        if(this.wrappedBehavior.getStatus() == Behavior.Status.RUNNING){
            this.wrappedBehavior.doStop(level, mob, gameTime);
        }
    }

    @Override
    public String debugString() {
        return "RunBehaviorIf(" + this.wrappedBehavior.debugString() + ")";
    }
}
