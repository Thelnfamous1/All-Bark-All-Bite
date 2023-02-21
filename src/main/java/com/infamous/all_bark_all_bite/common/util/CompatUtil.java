package com.infamous.all_bark_all_bite.common.util;

import net.minecraftforge.fml.ModList;

public class CompatUtil {
    public static final String REVAMPED_WOLF_MODID = "revampedwolf";

    public static boolean isRevampedWolfLoaded() {
       return ModList.get().isLoaded(REVAMPED_WOLF_MODID);
    }
}
