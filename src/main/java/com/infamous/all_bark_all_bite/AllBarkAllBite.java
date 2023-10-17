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
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AllBarkAllBite.MODID)
public class AllBarkAllBite
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "all_bark_all_bite";
    // Directly reference a slf4j logger
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean ENABLE_BRAIN_DEBUG = false; // Developers: Set this to true if necessary, otherwise should always be false when shipped to users

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

        getOrCreateDirectory(FMLPaths.CONFIGDIR.get().resolve(MODID), MODID);
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ABABConfig.COMMON_SPEC, String.format("%s/%s.toml", MODID, "common"));
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ABABConfig.CLIENT_SPEC, String.format("%s/%s.toml", MODID, "client"));
        modLoadingContext.registerConfig(ModConfig.Type.SERVER, ABABConfig.SERVER_SPEC, String.format("%s.toml", MODID));
    }

    /*
     * Copyright (c) Forge Development LLC and contributors
     * SPDX-License-Identifier: LGPL-2.1-only
     */
    private static Path getOrCreateDirectory(Path dirPath, String dirLabel) {
        if (!Files.isDirectory(dirPath.getParent())) {
            getOrCreateDirectory(dirPath.getParent(), "parent of " + dirLabel);
        }
        if (!Files.isDirectory(dirPath))
        {
            LOGGER.debug("Making {} directory : {}", dirLabel, dirPath);
            try {
                Files.createDirectory(dirPath);
            } catch (IOException e) {
                if (e instanceof FileAlreadyExistsException) {
                    LOGGER.error("Failed to create {} directory - there is a file in the way", dirLabel);
                } else {
                    LOGGER.error("Problem with creating {} directory (Permissions?)", dirLabel, e);
                }
                throw new RuntimeException("Problem creating directory", e);
            }
            LOGGER.debug("Created {} directory : {}", dirLabel, dirPath);
        } else {
            LOGGER.debug("Found existing {} directory : {}", dirLabel, dirPath);
        }
        return dirPath;
    }
}
