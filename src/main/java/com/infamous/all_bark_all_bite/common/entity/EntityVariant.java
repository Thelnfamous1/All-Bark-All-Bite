package com.infamous.all_bark_all_bite.common.entity;

import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import net.minecraft.resources.ResourceLocation;

public class EntityVariant {

    private final ResourceLocation texture;
    private final String entityPath;
    private final String variantPath;

    public EntityVariant(String namespace, String entityPath, String variantPath){
        this.texture = new ResourceLocation(namespace, MiscUtil.getEntityTexturePath(entityPath, variantPath));
        this.entityPath = entityPath;
        this.variantPath = variantPath;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public String getNamespace(){
        return this.texture.getNamespace();
    }

    public String getEntityPath() {
        return this.entityPath;
    }

    public String getVariantPath() {
        return this.variantPath;
    }
}
