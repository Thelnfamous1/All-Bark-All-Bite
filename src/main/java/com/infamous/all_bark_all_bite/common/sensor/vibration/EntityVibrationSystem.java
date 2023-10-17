package com.infamous.all_bark_all_bite.common.sensor.vibration;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class EntityVibrationSystem<E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>> implements VibrationSystem {

    private final Data vibrationData;
    private final VLC listenerConfig;

    public EntityVibrationSystem(Data vibrationData, VLC listenerConfig){
        this.vibrationData = vibrationData;
        this.listenerConfig = listenerConfig;
    }

    @Override
    public Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VLC getVibrationUser() {
        return this.listenerConfig;
    }

    @FunctionalInterface
    public interface Constructor<E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>>{
        EntityVibrationSystem<E, VLC> create(Data data, VLC listenerConfig);
    }
}
