package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ABABActivities {

    public static DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(ForgeRegistries.ACTIVITIES, AllBarkAllBite.MODID);

    private static final String FETCH_NAME = "fetch";
    public static RegistryObject<Activity> FETCH = ACTIVITIES.register(FETCH_NAME, () -> new Activity(FETCH_NAME));
}
