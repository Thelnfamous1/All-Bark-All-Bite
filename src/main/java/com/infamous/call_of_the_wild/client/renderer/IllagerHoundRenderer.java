package com.infamous.call_of_the_wild.client.renderer;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.client.ABABModelLayers;
import com.infamous.call_of_the_wild.client.renderer.model.IllagerHoundModel;
import com.infamous.call_of_the_wild.common.entity.illager_hound.IllagerHound;
import com.infamous.call_of_the_wild.common.registry.ABABEntityTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("NullableProblems")
public class IllagerHoundRenderer extends MobRenderer<IllagerHound, IllagerHoundModel<IllagerHound>> {
    private static final ResourceLocation ILLAGER_HOUND_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, String.format("textures/entity/illager/%s.png", ABABEntityTypes.ILLAGER_HOUND_NAME));

    public IllagerHoundRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerHoundModel<>(context.bakeLayer(ABABModelLayers.ILLAGER_HOUND)), 0.5F);
    }

    public ResourceLocation getTextureLocation(IllagerHound hound) {
        return ILLAGER_HOUND_LOCATION;
    }
}
