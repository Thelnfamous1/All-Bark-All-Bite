package com.infamous.all_bark_all_bite.common.vibration;

import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;

public class DogVibrationListenerConfig extends SharedWolfVibrationListenerConfig<Dog> {

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return ABABTags.DOG_CAN_LISTEN;
    }
}
