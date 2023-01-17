package com.infamous.call_of_the_wild.client.renderer;

import com.infamous.call_of_the_wild.client.ABABModelLayers;
import com.infamous.call_of_the_wild.client.renderer.model.IllagerHoundModelTemp;
import com.infamous.call_of_the_wild.common.entity.illager_hound.IllagerHound;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("NullableProblems")
public class IllagerHoundRenderer extends MobRenderer<IllagerHound, IllagerHoundModelTemp<IllagerHound>> {
    private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");
    private static final float ILLAGER_HOUND_SCALE = 1.5F;

    public IllagerHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerHoundModelTemp<>(context.bakeLayer(ABABModelLayers.ILLAGER_HOUND)), 0.5F);
    }

    @Override
    protected void scale(IllagerHound hound, PoseStack poseStack, float partialTick) {
        poseStack.scale(ILLAGER_HOUND_SCALE, ILLAGER_HOUND_SCALE, ILLAGER_HOUND_SCALE);
    }

    public ResourceLocation getTextureLocation(IllagerHound hound) {
        return WOLF_ANGRY_LOCATION;
    }
}
