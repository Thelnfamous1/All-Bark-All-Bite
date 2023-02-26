package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.function.Consumer;

public class AgeChangeTrigger<E extends LivingEntity> extends Behavior<E> {
    private boolean wasBaby;
    private final Consumer<E> onAgeChanged;

    public AgeChangeTrigger(Consumer<E> onAgeChanged) {
        super(ImmutableMap.of());
        this.onAgeChanged = onAgeChanged;
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        boolean baby = mob.isBaby();
        if(this.wasBaby != baby){
            this.onAgeChanged.accept(mob);
        }
        this.wasBaby = baby;
    }
}
