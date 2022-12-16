package com.infamous.call_of_the_wild.client;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("NullableProblems")
public class DogCollarLayer extends RenderLayer<Dog, AndreDogModel<Dog>> {
   private static final ResourceLocation DOG_COLLAR_LOCATION = new ResourceLocation(CallOfTheWild.MODID, "textures/entity/dog/dog_collar.png");
   private static final ResourceLocation DOG_TAG_LOCATION = new ResourceLocation(CallOfTheWild.MODID, "textures/entity/dog/dog_tag.png");

   public DogCollarLayer(RenderLayerParent<Dog, AndreDogModel<Dog>> layerParent) {
      super(layerParent);
   }

   public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Dog dog, float animPosDelta, float lerpAnimSpeed, float partialTicks, float bob, float lerpYRot, float lerpXRot) {
      if (dog.isTame() && !dog.isInvisible()) {
         float[] afloat = dog.getCollarColor().getTextureDiffuseColors();
         renderColoredCutoutModel(this.getParentModel(), DOG_COLLAR_LOCATION, poseStack, bufferSource, packedLight, dog, afloat[0], afloat[1], afloat[2]);
         if(dog.hasCustomName()) renderColoredCutoutModel(this.getParentModel(), DOG_TAG_LOCATION, poseStack, bufferSource, packedLight, dog, 1.0F, 1.0F, 1.0F);
      }
   }
}