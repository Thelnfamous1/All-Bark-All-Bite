package com.infamous.all_bark_all_bite.common.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrainMaker<E extends LivingEntity> {

    private final Brain<E> brain;
    private final Map<Activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>>> coreActivities = Maps.newHashMap();
    private final List<Activity> activities = Lists.newArrayList();

    public BrainMaker(Brain<E> brain){
        this.brain = brain;
    }

    public void initCoreActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> behaviors){
        this.coreActivities.put(activity, behaviors);
        this.coreActivities.forEach(this.brain::addActivity);
    }

    public void initActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> behaviors){
        this.activities.add(activity);
        this.brain.addActivity(activity, behaviors);
    }

    public void initActivityWithConditions(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> behaviors, Set<Pair<MemoryModuleType<?>, MemoryStatus>> entryCondtion){
        this.activities.add(activity);
        this.brain.addActivityWithConditions(activity, behaviors, entryCondtion);
    }

    public void initActivityWithMemoryGate(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> behaviors, MemoryModuleType<?> memory){
        this.activities.add(activity);
        this.brain.addActivityAndRemoveMemoriesWhenStopped(activity, behaviors, ImmutableSet.of(Pair.of(memory, MemoryStatus.VALUE_PRESENT)), ImmutableSet.of(memory));
    }

    public void initActivityWithConditionsAndRemovals(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> behaviors, Set<Pair<MemoryModuleType<?>, MemoryStatus>> entryCondtion, Set<MemoryModuleType<?>> exitErasedMemories){
        this.activities.add(activity);
        this.brain.addActivityAndRemoveMemoriesWhenStopped(activity, behaviors, entryCondtion, exitErasedMemories);
    }

    public void setDefaultActivity(Activity activity){
        this.brain.setDefaultActivity(activity);
    }

    public List<Activity> getActivities() {
        return this.activities;
    }

    public Brain<E> makeBrain(Activity defaultActivity) {
        this.brain.setCoreActivities(this.coreActivities.keySet());
        this.brain.setDefaultActivity(defaultActivity);
        this.brain.useDefaultActivity();
        return this.brain;
    }
}
