package com.infamous.all_bark_all_bite.data;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.item.AdjustableInstrumentItem;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABInstruments;
import com.infamous.all_bark_all_bite.common.registry.ABABItems;
import com.infamous.all_bark_all_bite.common.util.InstrumentUtil;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
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
        this.addWhistleWithDescription(ABABInstruments.ATTACK_WHISTLE, "Attack", "Pets will attack the targeted entity.");
        this.addWhistleWithDescription(ABABInstruments.COME_WHISTLE, "Come", "Pets will come to your position.");
        this.addWhistleWithDescription(ABABInstruments.FOLLOW_WHISTLE, "Follow", "Pets will follow you loosely.");
        this.addWhistleWithDescription(ABABInstruments.FREE_WHISTLE, "Free", "Pets will wander freely.");
        this.addWhistleWithDescription(ABABInstruments.GO_WHISTLE, "Go", "Pets will go to the targeted position.");
        this.addWhistleWithDescription(ABABInstruments.HEEL_WHISTLE, "Heel", "Pets will follow you closely.");
        this.addWhistleWithDescription(ABABInstruments.SIT_WHISTLE, "Sit", "Pets will sit down.");

        this.add(AdjustableInstrumentItem.SECONDARY_USE_TOOLTIP, "Sneak & Use to Adjust");
    }

    private void addWhistleWithDescription(RegistryObject<Instrument> whistle, String id, String description) {
        ResourceLocation location = whistle.getId();
        this.add(InstrumentUtil.makeInstrumentDescriptionId(location), id);
        this.add(InstrumentUtil.makeInstrumentDescription(location), description);
    }

}
