package com.infamous.all_bark_all_bite.common.compat;

import net.minecraftforge.fml.ModList;

public class CompatUtil {
    public static final String REVAMPED_WOLF_MODID = "revampedwolf";
    public static final String DOMESTICATION_INNOVATION_MODID = "domesticationinnovation";
    public static final String ILLAGERS_WEAR_ARMOR_MODID = "illagersweararmor";
    public static final String MUTANT_MORE_MODID = "mutantmore";

    public static boolean isRWLoaded() {
       return ModList.get().isLoaded(REVAMPED_WOLF_MODID);
    }

    public static boolean isDILoaded() {
        return ModList.get().isLoaded(DOMESTICATION_INNOVATION_MODID);
    }

    public static boolean isIWALoaded() {
        return ModList.get().isLoaded(ILLAGERS_WEAR_ARMOR_MODID);
    }

    public static boolean isMMLoaded(){
        return ModList.get().isLoaded(MUTANT_MORE_MODID);
    }

}
