package com.infamous.call_of_the_wild.client;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = CallOfTheWild.MODID, value = Dist.CLIENT)
public class ForgeClientEventHandler {

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    static void onRenderHighlight(RenderLevelStageEvent event){
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES){
            Camera camera = event.getCamera();
            Vec3 cameraPos = camera.getPosition();
            PoseStack poseStack = event.getPoseStack();
            //MultiBufferSource multiBufferSource = event.getMultiBufferSource();
            Minecraft.getInstance().debugRenderer.brainDebugRenderer.render(poseStack, null, cameraPos.x(), cameraPos.y(), cameraPos.z());
        }
    }
}
