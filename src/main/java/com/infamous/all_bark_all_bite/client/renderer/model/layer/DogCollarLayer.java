package com.infamous.all_bark_all_bite.client.renderer.model.layer;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.client.renderer.model.DogModel;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;

public class DogCollarLayer extends ABABWolfCollarLayer<Dog, DogModel<Dog>> {
   private static final ResourceLocation DOG_COLLAR_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, "textures/entity/dog/collar/collar.png");
   private static final ResourceLocation DOG_TAG_LOCATION = new ResourceLocation(AllBarkAllBite.MODID, "textures/entity/dog/collar/tag.png");

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