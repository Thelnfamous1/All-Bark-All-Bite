package com.infamous.all_bark_all_bite.client.renderer;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.client.ABABModelLayers;
import com.infamous.all_bark_all_bite.client.renderer.model.IllagerHoundModel;
import com.infamous.all_bark_all_bite.common.entity.illager_hound.IllagerHound;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class IllagerHoundRenderer extends MobRenderer<IllagerHound, IllagerHoundModel<IllagerHound>> {
    private static final ResourceLocation ILLAGER_HOUND_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, String.format("textures/entity/illager/%s.png", ABABEntityTypes.ILLAGER_HOUND_NAME));

    public IllagerHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerHoundModel<>(context.bakeLayer(ABABModelLayers.ILLAGER_HOUND)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(IllagerHound hound) {
        return ILLAGER_HOUND_LOCATION;
    }
}
