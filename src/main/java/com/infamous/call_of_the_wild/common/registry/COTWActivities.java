package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.CallOfTheWild;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class COTWActivities {

    public static DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(ForgeRegistries.ACTIVITIES, CallOfTheWild.MODID);

    private static final String FETCH_NAME = "fetch";
    public static RegistryObject<Activity> FETCH = ACTIVITIES.register(FETCH_NAME, () -> new Activity(FETCH_NAME));

    private static final String STALK_NAME = "stalk";
    public static RegistryObject<Activity> STALK = ACTIVITIES.register(STALK_NAME, () -> new Activity(STALK_NAME));

    private static final String HOWL_NAME = "howl";
    public static RegistryObject<Activity> HOWL = ACTIVITIES.register(HOWL_NAME, () -> new Activity(HOWL_NAME));
}
