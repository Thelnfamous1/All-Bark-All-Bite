package com.infamous.call_of_the_wild.client.renderer;

import com.infamous.call_of_the_wild.client.ABABModelLayers;
import com.infamous.call_of_the_wild.client.renderer.model.DogModel;
import com.infamous.call_of_the_wild.client.renderer.model.layer.DogCollarLayer;
import com.infamous.call_of_the_wild.client.renderer.model.layer.SharedWolfHeldItemLayer;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;

@SuppressWarnings("NullableProblems")
public class DogRenderer extends MobRenderer<Dog, DogModel<Dog>> {

   private static final float DEFAULT_SHADOW_RADIUS = 0.5F;

   public DogRenderer(EntityRendererProvider.Context context) {
      super(context, new DogModel<>(context.bakeLayer(ABABModelLayers.DOG)), DEFAULT_SHADOW_RADIUS);
      this.addLayer(new DogCollarLayer(this));
      this.addLayer(new SharedWolfHeldItemLayer<>(this, context.getItemInHandRenderer()));
   }

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
   protected void setupRotations(Dog dog, PoseStack poseStack, float ageInTicks, float lerpYBodyRot, float partialTick) {
      if (this.isShaking(dog)) {
         lerpYBodyRot += (float)(Math.cos((double)dog.tickCount * 3.25D) * Math.PI * (double)0.4F);
      }

      poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - lerpYBodyRot));

      if (dog.deathTime > 0) {
         float deathFlipProgress = ((float)dog.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
         deathFlipProgress = Mth.sqrt(deathFlipProgress);
         if (deathFlipProgress > 1.0F) {
            deathFlipProgress = 1.0F;
         }

         poseStack.mulPose(Vector3f.ZP.rotationDegrees(deathFlipProgress * this.getFlipDegrees(dog)));
      } else if (dog.isAutoSpinAttack()) {
         poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F - dog.getXRot()));
         poseStack.mulPose(Vector3f.YP.rotationDegrees(((float)dog.tickCount + partialTick) * -75.0F));
      } else if (!dog.hasPose(Pose.SLEEPING) && isEntityUpsideDown(dog)) {
         poseStack.translate(0.0D, dog.getBbHeight() + 0.1F, 0.0D);
         poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
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

   public ResourceLocation getTextureLocation(Dog dog) {
      return dog.getVariant().getTexture();
   }
}