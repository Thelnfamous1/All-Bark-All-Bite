package com.infamous.call_of_the_wild.common.sensor.vibration;

import com.google.common.collect.ImmutableSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;

import java.util.Set;

@SuppressWarnings("NullableProblems")
public class VibrationSensor<E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>> extends Sensor<E> {
    protected final EntityVibrationListenerConfig.Constructor<E, VLC> listenerConfigFactory;
    protected final MemoryModuleType<EntityVibrationListener<E, VLC>> listenerMemory;
    private DynamicGameEventListener<EntityVibrationListener<E, VLC>> dynamicVibrationListener;

    public VibrationSensor(EntityVibrationListenerConfig.Constructor<E, VLC> listenerConfigFactory, MemoryModuleType<EntityVibrationListener<E, VLC>> listenerMemory) {
        this.listenerConfigFactory = listenerConfigFactory;
        this.listenerMemory = listenerMemory;
    }

    @Override
    protected void doTick(ServerLevel level, E mob) {
        if(this.dynamicVibrationListener == null){
            EntityVibrationListener<E, VLC> listener = this.getListener(mob);
            this.dynamicVibrationListener = new DynamicGameEventListener<>(listener);
        }
        this.dynamicVibrationListener.move(level);
        this.dynamicVibrationListener.getListener().tick(level);
    }

    protected EntityVibrationListener<E, VLC> getListener(E mob) {
        Brain<?> brain = mob.getBrain();
        EntityVibrationListener<E, VLC> listener = brain.getMemory(this.listenerMemory).orElseGet(() -> this.createDefaultListener(mob));
        listener.getConfig().setEntity(mob);
        brain.setMemory(this.listenerMemory, listener);
        return listener;
    }

    protected EntityVibrationListener<E, VLC> createDefaultListener(E mob) {
        PositionSource positionSource = new EntityPositionSource(mob, mob.getEyeHeight());
        VLC listenerConfig = this.listenerConfigFactory.create();
        return new EntityVibrationListener<>(positionSource, this.defaultListenerRange(), listenerConfig, null, 0.0F, 0);
    }

    protected int defaultListenerRange() {
        return 64;
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(this.listenerMemory);
    }
}
