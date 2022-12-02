package com.infamous.call_of_the_wild.data;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.registry.COTWEntityTypes;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class COTWLangProvider extends LanguageProvider {
    public COTWLangProvider(DataGenerator gen) {
        super(gen, CallOfTheWild.MODID, "en_us");
    }

    public static COTWLangProvider create(DataGenerator gen){
        return new COTWLangProvider(gen);
    }

    @Override
    protected void addTranslations() {
        this.add(COTWEntityTypes.DOG.get(), "Dog");
    }
}
