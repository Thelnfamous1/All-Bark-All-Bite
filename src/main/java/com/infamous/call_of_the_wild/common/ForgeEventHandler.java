package com.infamous.call_of_the_wild.common;

import com.google.common.collect.Maps;
import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.entity.DogSpawner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = CallOfTheWild.MODID)
public class ForgeEventHandler {
    private static final Map<ResourceKey<Level>, List<CustomSpawner>> CUSTOM_SPAWNERS = Maps.newLinkedHashMap();

    static {
        ArrayList<CustomSpawner> overworldSpawners = Lists.newArrayList();
        overworldSpawners.add(new DogSpawner());
        CUSTOM_SPAWNERS.put(Level.OVERWORLD, overworldSpawners);
    }

    @SubscribeEvent
    static void onWorldTick(TickEvent.LevelTickEvent event){
        if(event.level instanceof ServerLevel serverLevel && event.phase == TickEvent.Phase.END){
            MinecraftServer server = serverLevel.getServer();
            ResourceKey<Level> dimension = serverLevel.dimension();
            List<CustomSpawner> customSpawners = CUSTOM_SPAWNERS.get(dimension);
            if(customSpawners != null) customSpawners.forEach(cs -> cs.tick(serverLevel, server.isSpawningMonsters(), server.isSpawningAnimals()));
        }
    }
}
