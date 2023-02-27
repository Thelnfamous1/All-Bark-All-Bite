package com.infamous.all_bark_all_bite.common.logic;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import net.minecraft.world.entity.raid.Raid;

import java.util.Arrays;
import java.util.List;

public class ABABRaiderTypes {
    private static final int[] HOUNDMASTER_SPAWNS_PER_WAVE_BEFORE_BONUS = new int[8];

    // Called during FMLCommonSetupEvent
    public static void initHoundmasterRaiderType(){
        Arrays.fill(HOUNDMASTER_SPAWNS_PER_WAVE_BEFORE_BONUS, 0);
        Raid.RaiderType.create(ABABEntityTypes.HOUNDMASTER_NAME, ABABEntityTypes.HOUNDMASTER.get(), HOUNDMASTER_SPAWNS_PER_WAVE_BEFORE_BONUS);
    }

    // Called during ServerAboutToStartEvent
    public static void refreshHoundmasterRaiderType() {
        List<? extends Integer> houndmasterRaidWaveCounts = ABABConfig.houndmasterRaidWaveCounts.get();
        for(int i = 1; i < HOUNDMASTER_SPAWNS_PER_WAVE_BEFORE_BONUS.length; i++){
            try{
                HOUNDMASTER_SPAWNS_PER_WAVE_BEFORE_BONUS[i] = houndmasterRaidWaveCounts.get(i - 1);
            } catch (Exception ignored){
                HOUNDMASTER_SPAWNS_PER_WAVE_BEFORE_BONUS[i] = 0;
            }
        }
        AllBarkAllBite.LOGGER.info("Added RaiderType for entity type {} with wave counts {}", ABABEntityTypes.HOUNDMASTER.get(), Arrays.toString(HOUNDMASTER_SPAWNS_PER_WAVE_BEFORE_BONUS));
    }
}
