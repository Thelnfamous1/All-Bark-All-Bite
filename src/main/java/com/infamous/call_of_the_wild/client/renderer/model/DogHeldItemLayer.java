package com.infamous.call_of_the_wild.client.renderer.model;

import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("NullableProblems")
public class DogHeldItemLayer extends RenderLayer<Dog, AndreDogModel<Dog>> {
   private final ItemInHandRenderer itemInHandRenderer;

   public DogHeldItemLayer(RenderLayerParent<Dog, AndreDogModel<Dog>> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
      super(renderLayerParent);
      this.itemInHandRenderer = itemInHandRenderer;
   }

   @Override
   public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Dog dog, float animPosDelta, float lerpAnimSpeed, float partialTicks, float bob, float lerpYRot, float lerpXRot) {
      boolean sleeping = dog.isSleeping();
      boolean baby = dog.isBaby();
      poseStack.pushPose();
      if (baby) {
         float babyScale = 0.75F;
         poseStack.scale(babyScale, babyScale, babyScale);
         poseStack.translate(0.0D, 0.5D, 0.209375F);
      }

      poseStack.translate(
              (this.getParentModel()).head.x / 16.0F,
              (this.getParentModel()).head.y / 16.0F,
              (this.getParentModel()).head.z / 16.0F);
      float headRollAngle = dog.getHeadRollAngle(partialTicks);
      poseStack.mulPose(Vector3f.ZP.rotation(headRollAngle));
      poseStack.mulPose(Vector3f.YP.rotationDegrees(lerpYRot));
      poseStack.mulPose(Vector3f.XP.rotationDegrees(lerpXRot));
      if (dog.isBaby()) {
         if (sleeping) {
            poseStack.translate(0.4F, 0.26F, 0.15F);
         } else {
            poseStack.translate(0.06F, 0.26F, -0.5D);
         }
      } else if (sleeping) {
         poseStack.translate(0.46F, 0.26F, 0.22F);
      } else {
         poseStack.translate(0.06F, 0.27F, -0.5D);
      }

      poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
      if (sleeping) {
         poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
      }

      ItemStack mainHandStack = dog.getItemInMouth();
      this.itemInHandRenderer.renderItem(dog, mainHandStack, ItemTransforms.TransformType.GROUND, false, poseStack, bufferSource, packedLight);
      poseStack.popPose();
   }
}