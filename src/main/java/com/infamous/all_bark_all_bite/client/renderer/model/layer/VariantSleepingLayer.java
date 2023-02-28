package com.infamous.all_bark_all_bite.client.renderer.model.layer;

import com.google.common.collect.Maps;
import com.infamous.all_bark_all_bite.common.entity.EntityVariant;
import com.infamous.all_bark_all_bite.common.entity.VariantMob;
import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public class VariantSleepingLayer<T extends LivingEntity & VariantMob, M extends EntityModel<T>> extends SleepingLayer<T, M> {
    private static final Map<EntityVariant, ResourceLocation> EYELIDS_CACHE = Maps.newHashMap();
    public VariantSleepingLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    protected ResourceLocation getTextureLocation(T entity) {
        return getVariantEyelidsTexture(entity.getVariant());
    }

    private static ResourceLocation getVariantEyelidsTexture(EntityVariant variant) {
        return EYELIDS_CACHE.computeIfAbsent(variant, k -> new ResourceLocation(variant.getNamespace(), getEyelidsTexturePath(variant)));
    }

    private static String getEyelidsTexturePath(EntityVariant variant) {
        return MiscUtil.getEntityTexturePath(variant.getEntityPath(), String.format("eyelids/%s", variant.getVariantPath()));
    }
}
