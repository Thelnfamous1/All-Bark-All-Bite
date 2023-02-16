package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.item.AdjustableInstrumentItem;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABInstruments;
import com.infamous.all_bark_all_bite.common.registry.ABABItems;
import com.infamous.all_bark_all_bite.common.util.InstrumentUtil;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Instrument;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;

public class ABABLangProvider extends LanguageProvider {
    public ABABLangProvider(DataGenerator gen) {
        super(gen, AllBarkAllBite.MODID, "en_us");
    }

    public static ABABLangProvider create(DataGenerator gen){
        return new ABABLangProvider(gen);
    }

    @Override
    protected void addTranslations() {
        this.add(ABABEntityTypes.DOG.get(), "Dog");
        this.add(ABABItems.DOG_SPAWN_EGG.get(), "Dog Spawn Egg");
        this.add(ABABEntityTypes.ILLAGER_HOUND.get(), "Scavenger");
        this.add(ABABItems.ILLAGER_HOUND_SPAWN_EGG.get(), "Scavenger Spawn Egg");
        this.add(ABABEntityTypes.HOUNDMASTER.get(), "Houndmaster");
        this.add(ABABItems.HOUNDMASTER_SPAWN_EGG.get(), "Houndmaster Spawn Egg");

        this.add(ABABItems.WHISTLE.get(), "Whistle");
        this.add(makeInstrumentDescriptionId(ABABInstruments.ATTACK_WHISTLE), "Attack");
        this.add(makeInstrumentDescriptionId(ABABInstruments.COME_WHISTLE), "Come");
        this.add(makeInstrumentDescriptionId(ABABInstruments.FOLLOW_WHISTLE), "Follow");
        this.add(makeInstrumentDescriptionId(ABABInstruments.FREE_WHISTLE), "Free");
        this.add(makeInstrumentDescriptionId(ABABInstruments.GO_WHISTLE), "Go");
        this.add(makeInstrumentDescriptionId(ABABInstruments.HEEL_WHISTLE), "Heel");
        this.add(makeInstrumentDescriptionId(ABABInstruments.SIT_WHISTLE), "Sit");

        this.add(AdjustableInstrumentItem.SECONDARY_USE_TOOLTIP, "Sneak & Use to Adjust");
    }

    @SuppressWarnings("SameParameterValue")
    private static String makeInstrumentDescriptionId(RegistryObject<Instrument> instrumentRegistryObject) {
        return Util.makeDescriptionId(InstrumentUtil.INSTRUMENT_TAG, instrumentRegistryObject.getId());
    }

}
