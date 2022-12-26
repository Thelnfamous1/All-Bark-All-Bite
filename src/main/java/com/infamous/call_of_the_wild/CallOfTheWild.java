package com.infamous.call_of_the_wild;

import com.infamous.call_of_the_wild.common.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CallOfTheWild.MODID)
public class CallOfTheWild
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "call_of_the_wild";
    // Directly reference a slf4j logger
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();
    public CallOfTheWild()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        COTWActivities.ACTIVITIES.register(modEventBus);
        COTWDogVariants.DOG_VARIANTS.register(modEventBus);
        COTWEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(modEventBus);
        COTWEntityTypes.ENTITY_TYPES.register(modEventBus);
        COTWMemoryModuleTypes.MEMORY_MODULE_TYPES.register(modEventBus);
        COTWSensorTypes.SENSOR_TYPES.register(modEventBus);
    }
}
