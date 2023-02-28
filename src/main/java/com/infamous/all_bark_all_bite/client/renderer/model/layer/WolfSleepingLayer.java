package com.infamous.all_bark_all_bite.client.renderer.model.layer;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.client.renderer.model.ABABWolfModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

public class WolfSleepingLayer extends SleepingLayer<Wolf, ABABWolfModel<Wolf>> {
    private static final ResourceLocation WOLF_SLEEPING_EYES = new ResourceLocation(AllBarkAllBite.MODID, "textures/entity/wolf/eyelids.png");

    public WolfSleepingLayer(RenderLayerParent<Wolf, ABABWolfModel<Wolf>> parent) {
        super(parent);
    }

    @Override
    protected ResourceLocation getTextureLocation(Wolf entity) {
        return WOLF_SLEEPING_EYES;
    }
}
