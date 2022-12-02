package com.infamous.call_of_the_wild.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public interface VariantMob {

    String VARIANT_TAG_NAME = "variant";

    default void addVariantSaveData(CompoundTag tag) {
         tag.putString(VARIANT_TAG_NAME, this.getVariantRegistry().getKey(this.getVariant()).toString());
     }

     default void readVariantSaveData(CompoundTag tag) {
         EntityVariant variant = this.getVariantRegistry().getValue(ResourceLocation.tryParse(tag.getString(VARIANT_TAG_NAME)));
         if (variant != null) {
             this.setVariant(variant);
         }
     }

    IForgeRegistry<EntityVariant> getVariantRegistry();

     EntityVariant getVariant();

     void setVariant(EntityVariant variant);
}
