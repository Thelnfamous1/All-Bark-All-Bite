package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.registry.COTWGameEvents;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.tags.GameEventTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class COTWGameEventTagsProvider extends GameEventTagsProvider {
    public COTWGameEventTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, CallOfTheWild.MODID, existingFileHelper);
    }

    public static COTWGameEventTagsProvider create(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        return new COTWGameEventTagsProvider(generator, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(COTWTags.DOG_CAN_LISTEN).add(COTWGameEvents.ENTITY_HOWL.get());
        this.tag(COTWTags.WOLF_CAN_LISTEN).add(COTWGameEvents.ENTITY_HOWL.get());
        this.tag(GameEventTags.VIBRATIONS).add(COTWGameEvents.ENTITY_HOWL.get());
        this.tag(GameEventTags.WARDEN_CAN_LISTEN).add(COTWGameEvents.ENTITY_HOWL.get());
    }
}
