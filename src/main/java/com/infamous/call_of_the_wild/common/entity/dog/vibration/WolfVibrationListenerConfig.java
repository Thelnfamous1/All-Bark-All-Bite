package com.infamous.call_of_the_wild.common.entity.dog.vibration;

import com.infamous.call_of_the_wild.common.COTWTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.gameevent.GameEvent;

@SuppressWarnings("NullableProblems")
public class WolfVibrationListenerConfig extends SharedWolfVibrationListenerConfig<Wolf> {

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return COTWTags.WOLF_CAN_LISTEN;
    }
}
