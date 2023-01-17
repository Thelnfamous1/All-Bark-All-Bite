package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.common.registry.ABABEntityTypes;
import com.infamous.call_of_the_wild.common.registry.ABABItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

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
    }
}
