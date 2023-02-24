package com.infamous.all_bark_all_bite.common.util;

import net.minecraftforge.fml.ModList;

public class CompatUtil {
    public static final String REVAMPED_WOLF_MODID = "revampedwolf";
    public static final String DOMESTICATION_INNOVATION_MODID = "domesticationinnovation";

    public static boolean isRevampedWolfLoaded() {
       return ModList.get().isLoaded(REVAMPED_WOLF_MODID);
    }

    public static boolean isDILoaded() {
        return ModList.get().isLoaded(DOMESTICATION_INNOVATION_MODID);
    }

}
