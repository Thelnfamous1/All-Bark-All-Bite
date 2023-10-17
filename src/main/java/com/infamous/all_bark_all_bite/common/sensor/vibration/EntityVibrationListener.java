package com.infamous.all_bark_all_bite.common.sensor.vibration;

import com.infamous.all_bark_all_bite.mixin.VibrationDataAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSelector;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EntityVibrationListener<E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>> extends VibrationSystem.Listener {

    private final EntityVibrationSystem<E, VLC> system;

    public EntityVibrationListener(VLC config, @Nullable VibrationInfo receivingEvent, VibrationSelector selector, int travelTimeInTicks) {
        this(new EntityVibrationSystem<>(VibrationDataAccessor.newVibrationData(receivingEvent, selector, travelTimeInTicks, true), config));
    }

    public EntityVibrationListener(EntityVibrationSystem<E, VLC> system){
        super(system);
        this.system = system;
    }

    public static <E extends LivingEntity, VLC extends EntityVibrationListenerConfig<E>> Codec<EntityVibrationListener<E, VLC>> codec(EntityVibrationListenerConfig.Constructor<E, VLC> listenerConfigFactory) {
        return RecordCodecBuilder.create((builder) ->
                builder.group(
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range").forGetter(VibrationSystem.Listener::getListenerRadius),
                        VibrationInfo.CODEC.optionalFieldOf("event").forGetter((evl) -> Optional.ofNullable(evl.getSystem().getVibrationData().getCurrentVibration())),
                        VibrationSelector.CODEC.fieldOf("selector").forGetter((evl) -> evl.getSystem().getVibrationData().getSelectionStrategy()),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter((evl) -> evl.getSystem().getVibrationData().getTravelTimeInTicks()))
                        .apply(builder,
                                (range, event, selector, eventDelay) ->
                                        new EntityVibrationListener<>(listenerConfigFactory.create(range), event.orElse(null), selector, eventDelay)));
    }

    public EntityVibrationSystem<E, VLC> getSystem() {
        return this.system;
    }

}
