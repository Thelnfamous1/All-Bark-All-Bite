package com.infamous.call_of_the_wild.client;

import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("NullableProblems")
public class DogRenderer extends MobRenderer<Dog, AndreDogModel<Dog>> {

   private static final float DEFAULT_SHADOW_RADIUS = 0.5F;

   public DogRenderer(EntityRendererProvider.Context context) {
      super(context, new AndreDogModel<>(context.bakeLayer(COTWModelLayers.DOG)), DEFAULT_SHADOW_RADIUS);
      this.addLayer(new DogCollarLayer(this));
      this.addLayer(new DogHeldItemLayer(this, context.getItemInHandRenderer()));
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

   @Override
   protected void scale(Dog dog, PoseStack poseStack, float p_115316_) {
      float scaleFactor = 1.0F;
      if (dog.isBaby()) {
         scaleFactor *= 0.5F;
         this.shadowRadius = DEFAULT_SHADOW_RADIUS * 0.5F;
      } else {
         this.shadowRadius = DEFAULT_SHADOW_RADIUS;
      }

      poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
   }

   public ResourceLocation getTextureLocation(Dog dog) {
      return dog.getVariant().getTexture();
   }
}