package com.infamous.all_bark_all_bite.common.sensor.vibration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import org.jetbrains.annotations.Nullable;

public abstract class EntityVibrationListenerConfig<E extends LivingEntity> implements VibrationListener.VibrationListenerConfig {
    protected E entity;

    /**
     * Must call this before allowing the EntityVibrationListenerConfig to receive any GameEvents!
     */
    public EntityVibrationListenerConfig<E> setEntity(E mob) {
        if(this.entity == null){
            this.entity = mob;
        } else{
            throw new IllegalStateException("Cannot change the existing entity for EntityVibrationListenerConfig!");
        }
        return this;
    }

    private void validateEntity() {
        if (this.entity == null) {
            throw new IllegalStateException("Cannot use EntityVibrationListenerConfig without setting the entity first!");
        }
    }

    @Override
    public final boolean shouldListen(ServerLevel level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context) {
        this.validateEntity();
        return this.shouldEntityListen(level, gameEventListener, blockPos, gameEvent, context);
    }

    protected abstract boolean shouldEntityListen(ServerLevel level, GameEventListener gameEventListener, BlockPos signalPos, GameEvent gameEvent, GameEvent.Context context);

    @Override
    public final void onSignalReceive(ServerLevel level, GameEventListener gameEventListener, BlockPos signalPos, GameEvent signalEvent, @Nullable Entity signalSender, @Nullable Entity signalSenderOwner, float signalDistance){
        this.validateEntity();
        this.onEntityReceiveSignal(level, gameEventListener, signalPos, signalEvent, signalSender, signalSenderOwner, signalDistance);
    }

    protected abstract void onEntityReceiveSignal(ServerLevel level, GameEventListener gameEventListener, BlockPos signalPos, GameEvent signalEvent, Entity signalSender, Entity signalSenderOwner, float signalDistance);

    @FunctionalInterface
    public interface Constructor<E extends LivingEntity, C extends EntityVibrationListenerConfig<E>>{
        C create();
    }
}
