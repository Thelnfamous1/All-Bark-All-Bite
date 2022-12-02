package com.infamous.call_of_the_wild.common.entity;

import net.minecraft.resources.ResourceLocation;

public class EntityVariant {

    private final ResourceLocation texture;

    public EntityVariant(String namespace, String entityPath, String texturePath){
        this.texture = new ResourceLocation(namespace, String.format("textures/entity/%s/%s.png", entityPath, texturePath));
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }
}
