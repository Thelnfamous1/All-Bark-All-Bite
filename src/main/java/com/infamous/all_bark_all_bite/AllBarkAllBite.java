package com.infamous.all_bark_all_bite;

import com.infamous.all_bark_all_bite.common.registry.*;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AllBarkAllBite.MODID)
public class AllBarkAllBite
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "all_bark_all_bite";
    // Directly reference a slf4j logger
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean ENABLE_BRAIN_DEBUG = true; // Developers: Set this to true if necessary, otherwise should always be false when shipped to users

    public AllBarkAllBite()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ABABActivities.ACTIVITIES.register(modEventBus);
        ABABDogVariants.DOG_VARIANTS.register(modEventBus);
        ABABEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(modEventBus);
        ABABEntityTypes.ENTITY_TYPES.register(modEventBus);
        ABABGameEvents.GAME_EVENTS.register(modEventBus);
        ABABInstruments.INSTRUMENTS.register(modEventBus);
        ABABItems.ITEMS.register(modEventBus);
        ABABMemoryModuleTypes.MEMORY_MODULE_TYPES.register(modEventBus);
        ABABSensorTypes.SENSOR_TYPES.register(modEventBus);
        ABABSoundEvents.SOUND_EVENTS.register(modEventBus);

        FileUtils.getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(MODID), MODID);
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ABABConfig.COMMON_SPEC, String.format("%s/%s.toml", MODID, "common"));
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ABABConfig.CLIENT_SPEC, String.format("%s/%s.toml", MODID, "client"));
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ABABConfig.SERVER_SPEC, String.format("%s.toml", MODID));
    }
}
