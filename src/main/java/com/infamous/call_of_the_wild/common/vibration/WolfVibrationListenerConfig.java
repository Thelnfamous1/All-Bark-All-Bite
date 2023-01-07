package com.infamous.call_of_the_wild.common.vibration;

import com.infamous.call_of_the_wild.common.ABABTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.gameevent.GameEvent;

@SuppressWarnings("NullableProblems")
public class WolfVibrationListenerConfig extends SharedWolfVibrationListenerConfig<Wolf> {

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return ABABTags.WOLF_CAN_LISTEN;
    }
}
