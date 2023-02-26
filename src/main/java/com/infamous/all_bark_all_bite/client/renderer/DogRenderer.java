package com.infamous.all_bark_all_bite.client.renderer;

import com.infamous.all_bark_all_bite.client.ABABModelLayers;
import com.infamous.all_bark_all_bite.client.renderer.model.DogModel;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.DogCollarLayer;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.SharedWolfHeldItemLayer;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DogRenderer extends MobRenderer<Dog, DogModel<Dog>> {

   private static final float DEFAULT_SHADOW_RADIUS = 0.5F;

   public DogRenderer(EntityRendererProvider.Context context) {
      super(context, new DogModel<>(context.bakeLayer(ABABModelLayers.DOG)), DEFAULT_SHADOW_RADIUS);
      this.addLayer(new DogCollarLayer(this));
      this.addLayer(new SharedWolfHeldItemLayer<>(this, context.getItemInHandRenderer()));
   }

   @Override
   public void render(Dog dog, float lerpYRot, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      if (dog.isWet()) {
         float wetShade = dog.getWetShade(partialTicks);
         this.model.setColor(wetShade, wetShade, wetShade);
      }

      super.render(dog, lerpYRot, partialTicks, poseStack, bufferSource, packedLight);
      if (dog.isWet()) {
         this.model.setColor(1.0F, 1.0F, 1.0F);
      }
   }

   @Override
   protected void scale(Dog dog, PoseStack poseStack, float partialTick) {
      float scaleFactor = 1.0F;
      if (dog.isBaby()) {
         scaleFactor *= 0.5F;
         this.shadowRadius = DEFAULT_SHADOW_RADIUS * 0.5F;
      } else {
         this.shadowRadius = DEFAULT_SHADOW_RADIUS;
      }

      poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
   }

   @Override
   public ResourceLocation getTextureLocation(Dog dog) {
      return dog.getVariant().getTexture();
   }
}