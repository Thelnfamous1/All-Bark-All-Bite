package com.infamous.call_of_the_wild.common.entity.dog.vibration;

import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.entity.dog.ai.Dog;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;

@SuppressWarnings("NullableProblems")
public class DogVibrationListenerConfig extends SharedWolfVibrationListenerConfig<Dog> {

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return ABABTags.DOG_CAN_LISTEN;
    }
}
