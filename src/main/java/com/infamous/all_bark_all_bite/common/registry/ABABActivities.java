package com.infamous.all_bark_all_bite.common.registry;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ABABActivities {

    public static DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(ForgeRegistries.ACTIVITIES, AllBarkAllBite.MODID);

    public static RegistryObject<Activity> FETCH = registerActivity("fetch");

    public static RegistryObject<Activity> COUNT_DOWN = registerActivity("count_down");

    public static RegistryObject<Activity> SIT = registerActivity("sit");

    public static RegistryObject<Activity> TARGET = registerActivity("target");

    public static RegistryObject<Activity> UPDATE = registerActivity("update");

    public static RegistryObject<Activity> HUNT = registerActivity("hunt");

    public static RegistryObject<Activity> FOLLOW = registerActivity("follow");

    private static RegistryObject<Activity> registerActivity(String name) {
        return ACTIVITIES.register(name, () -> new Activity(name));
    }

}
