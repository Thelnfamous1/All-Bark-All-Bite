package com.infamous.call_of_the_wild.client;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.registry.COTWEntityTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = CallOfTheWild.MODID, value = Dist.CLIENT)
public class ModClientEventHandler {
    private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
    private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(COTWModelLayers.DOG, AndreDogModel::createBodyLayer);
    }

    @SuppressWarnings("NullableProblems")
    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(COTWEntityTypes.DOG.get(), DogRenderer::new);
        event.registerEntityRenderer(EntityType.WOLF, context -> new WolfRenderer(context){
            @Override
            protected void scale(Wolf wolf, PoseStack poseStack, float partialTick) {
                float scaleFactor = 1.25F;
                poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
            }

            @Override
            public ResourceLocation getTextureLocation(Wolf wolf) {
                return wolf.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
            }
        });
    }
}
