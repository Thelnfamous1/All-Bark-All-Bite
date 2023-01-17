package com.infamous.call_of_the_wild.client.renderer;

import com.infamous.call_of_the_wild.client.ABABModelLayers;
import com.infamous.call_of_the_wild.common.entity.houndmaster.Houndmaster;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("NullableProblems")
public class HoundmasterRenderer extends IllagerRenderer<Houndmaster> {
    private static final ResourceLocation PILLAGER = new ResourceLocation("textures/entity/illager/pillager.png");

    public HoundmasterRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ABABModelLayers.HOUNDMASTER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    public ResourceLocation getTextureLocation(Houndmaster houndmaster) {
        return PILLAGER;
    }
}
