package com.infamous.call_of_the_wild.client;

import com.infamous.call_of_the_wild.common.entity.dog.ai.WolfAi;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.animal.Wolf;

@SuppressWarnings("NullableProblems")
class COTWWolfRenderer extends WolfRenderer {

    public COTWWolfRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(Wolf wolf, PoseStack poseStack, float partialTick) {
        poseStack.scale(WolfAi.WOLF_SIZE_SCALE, WolfAi.WOLF_SIZE_SCALE, WolfAi.WOLF_SIZE_SCALE);
    }
}
