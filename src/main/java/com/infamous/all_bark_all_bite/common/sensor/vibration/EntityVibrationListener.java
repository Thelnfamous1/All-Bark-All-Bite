package com.infamous.all_bark_all_bite.common.sensor.vibration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSelector;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EntityVibrationListener<E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>> extends VibrationListener {
    public EntityVibrationListener(PositionSource listenerSource, int listenerRange, VLC config, @Nullable VibrationInfo receivingEvent, VibrationSelector selector, int travelTimeInTicks) {
        super(listenerSource, listenerRange, config, receivingEvent, selector, travelTimeInTicks);
    }

    public static <E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>> Codec<EntityVibrationListener<E, VLC>> codec(EntityVibrationListenerConfig.Constructor<E, VLC> listenerConfigFactory) {
        return RecordCodecBuilder.create((builder) ->
                builder.group(
                        PositionSource.CODEC.fieldOf("source").forGetter((evl) -> evl.listenerSource),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range").forGetter((evl) -> evl.listenerRange),
                        VibrationInfo.CODEC.optionalFieldOf("event").forGetter((evl) -> Optional.ofNullable(evl.currentVibration)),
                        VibrationSelector.CODEC.fieldOf("selector").forGetter((evl) -> evl.selectionStrategy),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter((evl) -> evl.travelTimeInTicks))
                        .apply(builder,
                                (source, range, event, selector, eventDelay) ->
                                        new EntityVibrationListenerConfig(source, range, listenerConfigFactory.create(), event.orElse(null), selector, eventDelay)));
    }

    @SuppressWarnings("unchecked")
    public VLC getConfig(){
        return (VLC) this.config;
    }

}
