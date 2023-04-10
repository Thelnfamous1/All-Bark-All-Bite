package com.infamous.all_bark_all_bite.common.util.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infamous.all_bark_all_bite.common.util.ReflectionUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
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

    public static <E extends LivingEntity> ShufflingList<BehaviorControl<? super E>> getGateBehaviors(GateBehavior<E> gateBehavior){
        return ReflectionUtil.getField(GATE_BEHAVIOR_BEHAVIORS, GateBehavior.class, gateBehavior);
    }

    public static <E extends LivingEntity> ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> createPriorityPairs(int priorityStart, ImmutableList<? extends BehaviorControl<? super E>> behaviors) {
        int i = priorityStart;
        ImmutableList.Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> builder = ImmutableList.builder();

        for(BehaviorControl<? super E> behavior : behaviors) {
            builder.add(Pair.of(i++, behavior));
        }

        return builder.build();
    }

    public static <E extends LivingEntity> GateBehavior<E> tryAllBehaviorsInOrderIfAbsent(List<BehaviorControl<? super E>> behaviors, MemoryModuleType<?>... entryTypes){
        ImmutableMap.Builder<MemoryModuleType<?>, MemoryStatus> entryConditions = ImmutableMap.builder();
        for(MemoryModuleType<?> type : entryTypes){
            entryConditions.put(type, MemoryStatus.VALUE_ABSENT);
        }

        return new GateBehavior<>(
                entryConditions.build(),
                ImmutableSet.of(),
                GateBehavior.OrderPolicy.ORDERED,
                GateBehavior.RunningPolicy.TRY_ALL,
                basicWeightedBehaviors(behaviors));
    }

    private static <E extends LivingEntity> ImmutableList<Pair<? extends BehaviorControl<? super E>, Integer>> basicWeightedBehaviors(List<BehaviorControl<? super E>> behaviors){
        ImmutableList.Builder<Pair<? extends BehaviorControl<? super E>, Integer>> weightedBehaviors = ImmutableList.builder();
        for(BehaviorControl<? super E> behavior : behaviors){
            weightedBehaviors.add(Pair.of(behavior, 1));
        }
        return weightedBehaviors.build();
    }

    public static <E extends LivingEntity> Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> getSensors(Brain<E> brain){
        return ReflectionUtil.getField(BRAIN_SENSORS, Brain.class, brain);
    }

}
