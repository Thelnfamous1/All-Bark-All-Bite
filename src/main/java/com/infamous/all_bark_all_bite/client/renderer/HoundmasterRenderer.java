package com.infamous.all_bark_all_bite.client.renderer;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.client.ABABModelLayers;
import com.infamous.all_bark_all_bite.client.renderer.model.HoundmasterModel;
import com.infamous.all_bark_all_bite.common.entity.houndmaster.Houndmaster;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class HoundmasterRenderer extends IllagerRenderer<Houndmaster> {
    private static final float SHADOW_RADIUS = 0.5F;
    private static final ResourceLocation HOUNDMASTER = new ResourceLocation(AllBarkAllBite.MODID, "textures/entity/illager/houndmaster.png");

    public HoundmasterRenderer(EntityRendererProvider.Context context) {
        super(context, new HoundmasterModel<>(context.bakeLayer(ABABModelLayers.HOUNDMASTER)), SHADOW_RADIUS);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Houndmaster houndmaster) {
        return HOUNDMASTER;
    }

}
