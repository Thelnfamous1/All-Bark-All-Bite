package com.infamous.all_bark_all_bite.common.sensor.vibration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.Nullable;

public abstract class EntityVibrationListenerConfig<E extends LivingEntity> implements VibrationSystem.User {
    private final int listenerRadius;
    protected E entity;
    private EntityPositionSource positionSource;

    protected EntityVibrationListenerConfig(int listenerRadius){
        this.listenerRadius = listenerRadius;
    }

    @Override
    public EntityPositionSource getPositionSource() {
        return this.positionSource;
    }

    @Override
    public int getListenerRadius() {
        return this.listenerRadius;
    }

    /**
     * Must call this before allowing the EntityVibrationListenerConfig to receive any GameEvents!
     */
    public EntityVibrationListenerConfig<E> setEntity(E mob) {
        if(this.entity == null){
            this.entity = mob;
            this.positionSource = new EntityPositionSource(this.entity, this.entity.getEyeHeight());
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
    public final boolean canReceiveVibration(ServerLevel level, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context) {
        this.validateEntity();
        return this.shouldEntityListen(level, blockPos, gameEvent, context);
    }

    protected abstract boolean shouldEntityListen(ServerLevel level, BlockPos signalPos, GameEvent gameEvent, GameEvent.Context context);

    @Override
    public final void onReceiveVibration(ServerLevel level, BlockPos signalPos, GameEvent signalEvent, @Nullable Entity signalSender, @Nullable Entity signalSenderOwner, float signalDistance){
        this.validateEntity();
        this.onEntityReceiveSignal(level, signalPos, signalEvent, signalSender, signalSenderOwner, signalDistance);
    }

    protected abstract void onEntityReceiveSignal(ServerLevel level, BlockPos signalPos, GameEvent signalEvent, Entity signalSender, Entity signalSenderOwner, float signalDistance);

    @FunctionalInterface
    public interface Constructor<E extends LivingEntity, C extends EntityVibrationListenerConfig<E>>{
        C create(int listenerRadius);
    }
}
