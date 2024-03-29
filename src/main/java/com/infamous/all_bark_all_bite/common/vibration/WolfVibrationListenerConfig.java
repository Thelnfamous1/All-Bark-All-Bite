package com.infamous.all_bark_all_bite.common.vibration;

import com.infamous.all_bark_all_bite.common.ABABTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.gameevent.GameEvent;

public class WolfVibrationListenerConfig extends SharedWolfVibrationListenerConfig<Wolf> {

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return ABABTags.WOLF_CAN_LISTEN;
    }
}
