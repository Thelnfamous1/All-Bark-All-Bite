package com.infamous.call_of_the_wild.client;

import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DogRenderer extends MobRenderer<Dog, DogModel<Dog>> {

   public DogRenderer(EntityRendererProvider.Context context) {
      super(context, new DogModel<>(context.bakeLayer(COTWModelLayers.DOG)), 0.5F);
      this.addLayer(new DogCollarLayer(this));
   }

   protected float getBob(Dog dog, float p_116529_) {
      return dog.getTailAngle();
   }

   public void render(Dog dog, float p_116532_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_116536_) {
      if (dog.isWet()) {
         float wetShade = dog.getWetShade(partialTicks);
         this.model.setColor(wetShade, wetShade, wetShade);
      }

      super.render(dog, p_116532_, partialTicks, poseStack, bufferSource, p_116536_);
      if (dog.isWet()) {
         this.model.setColor(1.0F, 1.0F, 1.0F);
      }

   }

   public ResourceLocation getTextureLocation(Dog dog) {
      return dog.getVariant().getTexture();
   }
}