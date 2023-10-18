package com.infamous.all_bark_all_bite.common.compat;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = AllBarkAllBite.MODID)
public class CompatUtil {

    private static boolean RW_LOADED = false;
    private static boolean DI_LOADED = false;
    private static boolean IWA_LOADED = false;
    private static boolean MM_LOADED = false;
    private static boolean SM_LOADED = false;
    private static boolean WAYVF_LOADED = false;
    public static final String REVAMPED_WOLF_MODID = "revampedwolf";
    public static final String DOMESTICATION_INNOVATION_MODID = "domesticationinnovation";
    public static final String ILLAGERS_WEAR_ARMOR_MODID = "illagersweararmor";
    public static final String MUTANT_MORE_MODID = "mutantmore";

    public static final String FARMERS_DELIGHT_MODID = "farmersdelight";

    public static final String STORY_MOD_MODID = "storymod";
    public static final String WAYVF_MODID = "whatareyouvotingfor";

    public static boolean isRWLoaded() {
       return RW_LOADED;
    }

    public static boolean isDILoaded() {
        return DI_LOADED;
    }

    public static boolean isIWALoaded() {
        return IWA_LOADED;
    }

    public static boolean isMMLoaded(){
        return MM_LOADED;
    }

    public static boolean isSMLoaded() {
        return SM_LOADED;
    }

    @SubscribeEvent
    static void onLoadComplete(FMLLoadCompleteEvent event){
        RW_LOADED = ModList.get().isLoaded(REVAMPED_WOLF_MODID);
        DI_LOADED = ModList.get().isLoaded(DOMESTICATION_INNOVATION_MODID);
        IWA_LOADED = ModList.get().isLoaded(ILLAGERS_WEAR_ARMOR_MODID);
        MM_LOADED = ModList.get().isLoaded(MUTANT_MORE_MODID);
        SM_LOADED = ModList.get().isLoaded(STORY_MOD_MODID);
        WAYVF_LOADED = ModList.get().isLoaded(WAYVF_MODID);
    }

    public static boolean isWAYVFLoaded() {
        return WAYVF_LOADED;
    }
}
