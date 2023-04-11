package com.infamous.all_bark_all_bite.client.compat;

import baguchan.revampedwolf.api.IWolfTypes;
import baguchan.revampedwolf.api.WolfTypes;
import com.infamous.all_bark_all_bite.client.ABABModelLayers;
import com.infamous.all_bark_all_bite.client.renderer.model.ABABWolfModel;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.RWWolfArmorLayer;
import com.infamous.all_bark_all_bite.common.compat.CompatUtil;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RWCompatClient {

    public static RenderLayer<Wolf, ABABWolfModel<Wolf>> getRWArmorLayer(RenderLayerParent<Wolf, ABABWolfModel<Wolf>> renderLayerParent, EntityRendererProvider.Context context) {
        return new RWWolfArmorLayer<>(renderLayerParent, new ABABWolfModel<>(context.getModelSet().bakeLayer(ABABModelLayers.RW_WOLF_ARMOR)));
    }

    private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
    private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
    private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

    private static final Map<WolfTypes, MutableTriple<ResourceLocation, ResourceLocation, ResourceLocation>> WOLF_TEXTURE_CACHE = new HashMap<>();

    public static ResourceLocation getTextureLocation(Wolf wolf) {
        IWolfTypes wolfTypes = (IWolfTypes)wolf;
        if (wolfTypes.getVariant() == WolfTypes.WHITE) {
            if (wolf.isTame()) {
                return WOLF_TAME_LOCATION;
            } else {
                return wolf.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
            }
        } else {
            MutableTriple<ResourceLocation, ResourceLocation, ResourceLocation> textures = WOLF_TEXTURE_CACHE
                    .computeIfAbsent(wolfTypes.getVariant(), k -> new MutableTriple<>());
            if (wolf.isTame()) {
                if(textures.left == null)
                    textures.setLeft(buildWolfVariantTextureLocation(wolfTypes, "_tame.png"));
                return textures.left;
            } else if (wolf.isAngry()) {
                if(textures.middle == null)
                    textures.setMiddle(buildWolfVariantTextureLocation(wolfTypes, "_angry.png"));
                return textures.middle;
            } else {
                if(textures.right == null)
                    textures.setRight(buildWolfVariantTextureLocation(wolfTypes, ".png"));
                return textures.right;
            }
        }
    }

    @NotNull
    private static ResourceLocation buildWolfVariantTextureLocation(IWolfTypes wolfTypes, String x) {
        return new ResourceLocation(CompatUtil.REVAMPED_WOLF_MODID, "textures/entity/wolf/wolf_" + wolfTypes.getVariant().type + x);
    }
}
