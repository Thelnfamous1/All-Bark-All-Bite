package com.infamous.call_of_the_wild.client.renderer;

import com.infamous.call_of_the_wild.common.entity.wolf.WolfAi;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.world.entity.animal.Wolf;

@SuppressWarnings("NullableProblems")
public class ABABWolfRenderer extends WolfRenderer {

    public ABABWolfRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(Wolf wolf, PoseStack poseStack, float partialTick) {
        poseStack.scale(WolfAi.WOLF_SIZE_SCALE, WolfAi.WOLF_SIZE_SCALE, WolfAi.WOLF_SIZE_SCALE);
    }
}
