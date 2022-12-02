package com.infamous.call_of_the_wild.client;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.registry.COTWEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = CallOfTheWild.MODID, value = Dist.CLIENT)
public class ModClientEventHandler {

    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(COTWModelLayers.DOG, DogModel::createBodyLayer);
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(COTWEntityTypes.DOG.get(), DogRenderer::new);
    }
}
