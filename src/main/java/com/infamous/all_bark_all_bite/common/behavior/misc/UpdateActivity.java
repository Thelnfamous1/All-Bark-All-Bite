package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.function.BiConsumer;

public class UpdateActivity<E extends LivingEntity> extends Behavior<E> {
    private final List<Activity> activities;
    private final BiConsumer<E, Pair<Activity, Activity>> onActivityChanged;

    public UpdateActivity(List<Activity> activities, BiConsumer<E, Pair<Activity, Activity>> onActivityChanged) {
        super(ImmutableMap.of());
        this.activities = activities;
        this.onActivityChanged = onActivityChanged;
    }

    @Override
    protected void start(ServerLevel level, E entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        Activity previous = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(this.activities);
        Activity current = brain.getActiveNonCoreActivity().orElse(null);
        if (previous != current) {
            this.onActivityChanged.accept(entity, Pair.of(previous, current));
        }
    }
}
