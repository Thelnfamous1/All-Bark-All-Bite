package com.infamous.call_of_the_wild.common.vibration;

import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;

public class DogVibrationListenerConfig extends SharedWolfVibrationListenerConfig<Dog> {

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return ABABTags.DOG_CAN_LISTEN;
    }
}
