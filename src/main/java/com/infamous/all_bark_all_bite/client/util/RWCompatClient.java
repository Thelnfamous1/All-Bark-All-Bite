package com.infamous.all_bark_all_bite.client.util;

import com.infamous.all_bark_all_bite.client.ABABModelLayers;
import com.infamous.all_bark_all_bite.client.renderer.model.ABABWolfModel;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.RWWolfArmorLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.Wolf;

public class RWCompatClient {

    public static RenderLayer<Wolf, ABABWolfModel<Wolf>> getRWArmorLayer(RenderLayerParent<Wolf, ABABWolfModel<Wolf>> renderLayerParent, EntityRendererProvider.Context context) {
        return new RWWolfArmorLayer<>(renderLayerParent, new ABABWolfModel<>(context.getModelSet().bakeLayer(ABABModelLayers.RW_WOLF_ARMOR)));
    }
}
