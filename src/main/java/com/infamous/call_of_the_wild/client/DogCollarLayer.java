package com.infamous.call_of_the_wild.client;

import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class DogCollarLayer extends RenderLayer<Dog, DogModel<Dog>> {
   private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation(CallOfTheWild.MODID, "textures/entity/dog/dog_collar.png");

   public DogCollarLayer(RenderLayerParent<Dog, DogModel<Dog>> layerParent) {
      super(layerParent);
   }

   public void render(PoseStack poseStack, MultiBufferSource bufferSource, int p_117722_, Dog dog, float p_117724_, float p_117725_, float p_117726_, float p_117727_, float p_117728_, float p_117729_) {
      if (dog.isTame() && !dog.isInvisible()) {
         float[] afloat = dog.getCollarColor().getTextureDiffuseColors();
         renderColoredCutoutModel(this.getParentModel(), WOLF_COLLAR_LOCATION, poseStack, bufferSource, p_117722_, dog, afloat[0], afloat[1], afloat[2]);
      }
   }
}