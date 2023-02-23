package com.infamous.all_bark_all_bite.common.event;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * MakeBrain is fired AFTER an entity's {@link LivingEntity#brain} field is changed.
 * If a method utilizes this Event as its parameter, the method will receive every child event of this class.
 * <p>
 * The entity's original {@link Brain} will be replaced by {@link BrainEvent#getNewBrain()} after the event is posted.
 * You may manipulate the new Brain instance stored in {@link BrainEvent#newBrain} through the getter and setter methods.
 * By default, the new Brain is the entity's original Brain obtained from {@link LivingEntity#getBrain()}.
 * <p>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class BrainEvent extends LivingEvent {
    private Brain<?> newBrain;

    public BrainEvent(LivingEntity entity) {
        super(entity);
        this.newBrain = entity.getBrain();
    }

    /**
     * Helper method to create a new {@link T}-typed {@link Brain.Provider} instance
     */
    public static <T extends LivingEntity> Brain.Provider<?> brainProvider(Collection<? extends MemoryModuleType<?>> memoryTypes, Collection<? extends SensorType<? extends Sensor<? super T>>> sensorTypes) {
        return Brain.provider(memoryTypes, sensorTypes);
    }

    /**
     * Helper method to create a {@link Tag}-typed {@link Dynamic} instance using {@link NbtOps#INSTANCE} and an empty map of memories.
     */
    public static Dynamic<Tag> makeDynamic() {
        NbtOps nbtOps = NbtOps.INSTANCE;
        return new Dynamic<>(nbtOps, nbtOps.createMap(ImmutableMap.of(nbtOps.createString("memories"), nbtOps.emptyMap())));
    }

    @SuppressWarnings("unchecked")
    public <T extends LivingEntity> Brain<T> getNewBrain() {
        return (Brain<T>) this.newBrain;
    }

    public void setNewBrain(Brain<?> brain){
        this.newBrain = brain;
    }

    /**
     * This event is fired AFTER an entity's {@link LivingEntity#brain} is set using {@link LivingEntity#makeBrain(Dynamic)}.
     * This allows you to remake an entity's Brain.
     * You may access a copy of the "Brain" {@link Tag} used to deserialize memory values if available.
     * <p>
     * This event is fired at the end of the constructor of {@link LivingEntity}, on both logical sides.
     * This event is fired at the end of {@link LivingEntity#readAdditionalSaveData(CompoundTag)}, only on the server side.
     * <p>
     * This event is not {@link Cancelable}.
     * This event does not have a result. {@link Event.HasResult}
     */
    public static class MakeBrain extends BrainEvent {
        @Nullable
        private final Tag brainTag;

        public MakeBrain(LivingEntity entity) {
            this(entity, null);
        }

        public MakeBrain(LivingEntity entity, @Nullable Tag brainTag){
            super(entity);
            if(brainTag != null) brainTag = brainTag.copy();
            this.brainTag = brainTag;
        }

        /**
         * Helper method to create a new {@link T}-typed {@link Brain} instance, using {@link MakeBrain#brainTag} if available.
         */
        @SuppressWarnings("unchecked")
        public <T extends LivingEntity> Brain<T> makeBrain(Collection<? extends MemoryModuleType<?>> memoryTypes, Collection<? extends SensorType<? extends Sensor<? super T>>> sensorTypes) {
            Dynamic<Tag> dynamic = this.brainTag == null ? makeDynamic() : new Dynamic<>(NbtOps.INSTANCE, this.brainTag);
            return (Brain<T>) brainProvider(memoryTypes, sensorTypes).makeBrain(dynamic);
        }
    }

}
