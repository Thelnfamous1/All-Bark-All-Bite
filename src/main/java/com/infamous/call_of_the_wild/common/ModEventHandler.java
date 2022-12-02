package com.infamous.call_of_the_wild.common;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.infamous.call_of_the_wild.common.registry.COTWEntityTypes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = CallOfTheWild.MODID)
public class ModEventHandler {

    @SubscribeEvent
    static void onEntityAttributeCreation(EntityAttributeCreationEvent event){
        event.put(COTWEntityTypes.DOG.get(), Dog.createAttributes().build());
    }
}
