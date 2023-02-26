package com.infamous.all_bark_all_bite.client;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.client.renderer.ABABWolfRenderer;
import com.infamous.all_bark_all_bite.client.renderer.DogRenderer;
import com.infamous.all_bark_all_bite.client.renderer.HoundmasterRenderer;
import com.infamous.all_bark_all_bite.client.renderer.IllagerHoundRenderer;
import com.infamous.all_bark_all_bite.client.renderer.model.ABABWolfModel;
import com.infamous.all_bark_all_bite.client.renderer.model.DogModel;
import com.infamous.all_bark_all_bite.client.renderer.model.HoundmasterModel;
import com.infamous.all_bark_all_bite.client.renderer.model.IllagerHoundModel;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = AllBarkAllBite.MODID, value = Dist.CLIENT)
public class ModClientEventHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(ABABModelLayers.DOG, DogModel::createBodyLayer);
        event.registerLayerDefinition(ABABModelLayers.WOLF, ABABWolfModel::createBodyLayer);
        event.registerLayerDefinition(ABABModelLayers.HOUNDMASTER, HoundmasterModel::createBodyLayer);
        event.registerLayerDefinition(ABABModelLayers.ILLAGER_HOUND, IllagerHoundModel::createBodyLayer);
        event.registerLayerDefinition(ABABModelLayers.RW_WOLF_ARMOR, ABABWolfModel::createBodyLayer);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(ABABEntityTypes.DOG.get(), DogRenderer::new);
        event.registerEntityRenderer(EntityType.WOLF, ABABWolfRenderer::new);
        event.registerEntityRenderer(ABABEntityTypes.HOUNDMASTER.get(), HoundmasterRenderer::new);
        event.registerEntityRenderer(ABABEntityTypes.ILLAGER_HOUND.get(), IllagerHoundRenderer::new);
    }

}
