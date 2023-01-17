package com.infamous.call_of_the_wild.common.ai;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.util.ReflectionUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class BrainUtil {

    private static final String GATE_BEHAVIOR_BEHAVIORS = "f_22871_";
    private static final String BRAIN_AVAILABLE_BEHAVIORS_BY_PRIORITY = "f_21845_";
    private static final String BRAIN_SENSORS = "f_21844_";

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> Brain<T> getTypedBrain(T mob) {
        return (Brain<T>) mob.getBrain();
    }

    public static <E extends LivingEntity> Map<Integer, Map<Activity, Set<Behavior<? super E>>>> getAvailableBehaviorsByPriority(Brain<E> brain){
        return ReflectionUtil.getField(BRAIN_AVAILABLE_BEHAVIORS_BY_PRIORITY, Brain.class, brain);
    }

    public static <E extends LivingEntity> ShufflingList<Behavior<? super E>> getGateBehaviors(GateBehavior<E> gateBehavior){
        return ReflectionUtil.getField(GATE_BEHAVIOR_BEHAVIORS, GateBehavior.class, gateBehavior);
    }

    public static <E extends LivingEntity> ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> createPriorityPairs(int priorityStart, ImmutableList<? extends Behavior<? super E>> behaviors) {
        int i = priorityStart;
        ImmutableList.Builder<Pair<Integer, ? extends Behavior<? super E>>> builder = ImmutableList.builder();

        for(Behavior<? super E> behavior : behaviors) {
            builder.add(Pair.of(i++, behavior));
        }

        return builder.build();
    }

    public static <E extends LivingEntity> GateBehavior<E> gateBehaviors(Map<MemoryModuleType<?>, MemoryStatus> entryCondition, Set<MemoryModuleType<?>> exitErasedMemories, GateBehavior.OrderPolicy orderPolicy, GateBehavior.RunningPolicy runningPolicy, ImmutableList<Pair<Behavior<? super E>, Integer>> behaviors){
        return new GateBehavior<>(
                entryCondition,
                exitErasedMemories,
                orderPolicy,
                runningPolicy,
                behaviors);
    }

    public static <E extends LivingEntity> Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> getSensors(Brain<E> brain){
        return ReflectionUtil.getField(BRAIN_SENSORS, Brain.class, brain);
    }
}
