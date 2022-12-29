package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unchecked")
public class BrainUtil {

    private static final String LIVING_ENTITY_BRAIN = "f_20939_";
    private static final String GATE_BEHAVIOR_BEHAVIORS = "f_22871_";
    private static final String BRAIN_AVAILABLE_BEHAVIORS_BY_PRIORITY = "f_21845_";

    public static <T extends LivingEntity> Brain.Provider<?> brainProvider(Collection<? extends MemoryModuleType<?>> memoryTypes, Collection<? extends SensorType<? extends Sensor<? super T>>> sensorTypes) {
        return Brain.provider(memoryTypes, sensorTypes);
    }

    public static <T extends LivingEntity> Brain<T> makeBrain(Collection<? extends MemoryModuleType<?>> memoryTypes, Collection<? extends SensorType<? extends Sensor<? super T>>> sensorTypes, Dynamic<?> dynamic) {
        return (Brain<T>) brainProvider(memoryTypes, sensorTypes).makeBrain(dynamic);
    }

    public static <T extends LivingEntity> void replaceBrain(T livingEntity, ServerLevel level, Brain<T> replacement, boolean merge) {
        Brain<T> original = (Brain<T>) livingEntity.getBrain();
        original.stopAll(level, livingEntity);
        if(merge) mergeMemories(original, replacement);
        ReflectionUtil.setField(LIVING_ENTITY_BRAIN, LivingEntity.class, livingEntity, replacement);
    }

    @SuppressWarnings("deprecation")
    public static void mergeMemories(Brain<?> original, Brain<?> replacement) {
        Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> originalMemories = original.getMemories();
        Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> replacementMemories = replacement.getMemories();

        originalMemories.forEach((k, v) -> {
            if (v.isPresent()) {
                replacementMemories.put(k, v);
            }
        });
    }

    public static Dynamic<Tag> makeDynamic(NbtOps nbtOps) {
        return new Dynamic<>(nbtOps, nbtOps.createMap(ImmutableMap.of(nbtOps.createString("memories"), nbtOps.emptyMap())));
    }

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
}
