package com.infamous.call_of_the_wild.client.renderer;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.client.ABABModelLayers;
import com.infamous.call_of_the_wild.client.renderer.model.IllagerHoundModel;
import com.infamous.call_of_the_wild.common.entity.illager_hound.IllagerHound;
import com.infamous.call_of_the_wild.common.registry.ABABEntityTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("NullableProblems")
public class IllagerHoundRenderer extends MobRenderer<IllagerHound, IllagerHoundModel<IllagerHound>> {
    private static final ResourceLocation ILLAGER_HOUND_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, String.format("textures/entity/illager/%s.png", ABABEntityTypes.ILLAGER_HOUND_NAME));
    private static final float ILLAGER_HOUND_SCALE = 1.5F;

    public IllagerHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerHoundModel<>(context.bakeLayer(ABABModelLayers.ILLAGER_HOUND)), 0.5F);
    }

    @Override
    protected void scale(IllagerHound hound, PoseStack poseStack, float partialTick) {
        //poseStack.scale(ILLAGER_HOUND_SCALE, ILLAGER_HOUND_SCALE, ILLAGER_HOUND_SCALE);
    }

    public ResourceLocation getTextureLocation(IllagerHound hound) {
        return ILLAGER_HOUND_LOCATION;
    }
}
