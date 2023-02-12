package com.infamous.all_bark_all_bite.common.sensor.vibration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EntityVibrationListener<E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>> extends VibrationListener {
    public EntityVibrationListener(PositionSource listenerSource, int listenerRange, VLC config, @Nullable VibrationListener.ReceivingEvent receivingEvent, float receivingDistance, int travelTimeInTicks) {
        super(listenerSource, listenerRange, config, receivingEvent, receivingDistance, travelTimeInTicks);
    }

    public static <E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>> Codec<EntityVibrationListener<E, VLC>> codec(EntityVibrationListenerConfig.Constructor<E, VLC> listenerConfigFactory) {
        return RecordCodecBuilder.create((instance) ->
                instance.group(PositionSource.CODEC.fieldOf("source")
                        .forGetter((listener) -> listener.listenerSource), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range")
                        .forGetter((listener) -> listener.listenerRange), ReceivingEvent.CODEC.optionalFieldOf("event")
                        .forGetter((listener) -> Optional.ofNullable(listener.receivingEvent)), Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("event_distance").orElse(0.0F)
                        .forGetter((listener) -> listener.receivingDistance), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0)
                        .forGetter((listener) -> listener.travelTimeInTicks))
                        .apply(instance, (listenerSource, listenerRange, receivingEvent, receivingDistance, travelTimeInTicks) ->
                                new EntityVibrationListener<>(listenerSource, listenerRange, listenerConfigFactory.create(), receivingEvent.orElse(null), receivingDistance, travelTimeInTicks)));
    }

    @SuppressWarnings("unchecked")
    public VLC getConfig(){
        return (VLC) this.config;
    }

}
