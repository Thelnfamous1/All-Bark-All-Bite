package com.infamous.all_bark_all_bite.client.compat;

import com.infamous.all_bark_all_bite.client.ABABModelLayers;
import com.infamous.all_bark_all_bite.client.renderer.model.ABABWolfModel;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.WAYVFWolfArmorLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.Wolf;

public class WAYVFCompatClient {

    public static RenderLayer<Wolf, ABABWolfModel<Wolf>> getWAYVFArmorLayer(RenderLayerParent<Wolf, ABABWolfModel<Wolf>> renderLayerParent, EntityRendererProvider.Context context) {
        return new WAYVFWolfArmorLayer<>(renderLayerParent, new ABABWolfModel<>(context.getModelSet().bakeLayer(ABABModelLayers.WAYVF_WOLF_ARMOR)));
    }
}
