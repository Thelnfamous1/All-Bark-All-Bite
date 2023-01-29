package com.infamous.call_of_the_wild.client.renderer.model.layer;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.client.renderer.model.DogModel;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;

public class DogCollarLayer extends SharedWolfCollarLayer<Dog, DogModel<Dog>> {
   private static final ResourceLocation DOG_COLLAR_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, "textures/entity/dog/dog_collar.png");
   private static final ResourceLocation DOG_TAG_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, "textures/entity/dog/dog_tag.png");

   public DogCollarLayer(RenderLayerParent<Dog, DogModel<Dog>> layerParent) {
      super(layerParent);
   }

   @Override
   protected void renderCollar(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Dog dog) {
      super.renderCollar(poseStack, bufferSource, packedLight, dog);
      if(dog.hasCustomName()) renderColoredCutoutModel(this.getParentModel(), this.getTagLocation(), poseStack, bufferSource, packedLight, dog, 1.0F, 1.0F, 1.0F);
   }

   @Override
   public ResourceLocation getCollarLocation() {
      return DOG_COLLAR_LOCATION;
   }

   public ResourceLocation getTagLocation() {
      return DOG_TAG_LOCATION;
   }
}