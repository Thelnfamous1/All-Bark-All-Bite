package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.registry.ABABGameEvents;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.tags.GameEventTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ABABGameEventTagsProvider extends GameEventTagsProvider {
    public ABABGameEventTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator, AllBarkAllBite.MODID, existingFileHelper);
    }

    public static ABABGameEventTagsProvider create(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        return new ABABGameEventTagsProvider(generator, existingFileHelper);
    }

    @Override
    protected void addTags() {
        this.tag(ABABTags.DOG_CAN_LISTEN).add(ABABGameEvents.ENTITY_HOWL.get());
        this.tag(ABABTags.WOLF_CAN_LISTEN).add(ABABGameEvents.ENTITY_HOWL.get());
        this.tag(GameEventTags.VIBRATIONS).add(ABABGameEvents.ENTITY_HOWL.get());
        this.tag(GameEventTags.WARDEN_CAN_LISTEN).add(ABABGameEvents.ENTITY_HOWL.get());
    }
}
