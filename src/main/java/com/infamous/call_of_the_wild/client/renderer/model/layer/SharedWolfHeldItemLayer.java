package com.infamous.call_of_the_wild.client.renderer.model.layer;

import com.infamous.call_of_the_wild.common.entity.InterestedMob;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("NullableProblems")
public class SharedWolfHeldItemLayer<T extends TamableAnimal, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
   private final ItemInHandRenderer itemInHandRenderer;

   public SharedWolfHeldItemLayer(RenderLayerParent<T, M> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
      super(renderLayerParent);
      this.itemInHandRenderer = itemInHandRenderer;
   }

   @Override
   public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T tamableAnimal, float animPosDelta, float lerpAnimSpeed, float partialTicks, float bob, float lerpYRot, float lerpXRot) {
      boolean sleeping = tamableAnimal.isSleeping();
      boolean baby = tamableAnimal.isBaby();
      poseStack.pushPose();
      if (baby) {
         float babyScale = 0.75F;
         poseStack.scale(babyScale, babyScale, babyScale);
         poseStack.translate(0.0D, 0.5D, 0.209375F);
      }

      poseStack.translate(
              (this.getParentModel()).getHead().x / 16.0F,
              (this.getParentModel()).getHead().y / 16.0F,
              (this.getParentModel()).getHead().z / 16.0F);
      float headRollAngle = getHeadRollAngle(tamableAnimal, partialTicks);
      poseStack.mulPose(Vector3f.ZP.rotation(headRollAngle));
      poseStack.mulPose(Vector3f.YP.rotationDegrees(lerpYRot));
      poseStack.mulPose(Vector3f.XP.rotationDegrees(lerpXRot));
      if (tamableAnimal.isBaby()) {
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

      ItemStack mainHandStack = tamableAnimal.getMainHandItem();
      this.itemInHandRenderer.renderItem(tamableAnimal, mainHandStack, ItemTransforms.TransformType.GROUND, false, poseStack, bufferSource, packedLight);
      poseStack.popPose();
   }

   private float getHeadRollAngle(T tamableAnimal, float partialTicks) {
      return tamableAnimal instanceof Wolf wolf ?
              wolf.getHeadRollAngle(partialTicks) : tamableAnimal instanceof InterestedMob interestedMob ?
              interestedMob.getHeadRollAngle(partialTicks) : 0.0F;
   }
}