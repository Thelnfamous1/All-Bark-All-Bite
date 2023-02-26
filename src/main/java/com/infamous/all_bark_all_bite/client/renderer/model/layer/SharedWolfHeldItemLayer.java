package com.infamous.all_bark_all_bite.client.renderer.model.layer;

import com.infamous.all_bark_all_bite.common.entity.InterestedMob;
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
         poseStack.translate(0.0D, 0.65D, 0.0D);
      }

      float headPosScale = 16.0F;
      poseStack.translate(
              (this.getParentModel()).getHead().x / headPosScale,
              (this.getParentModel()).getHead().y / headPosScale,
              (this.getParentModel()).getHead().z / headPosScale);
      float headRollAngle = getHeadRollAngle(tamableAnimal, partialTicks);
      poseStack.mulPose(Vector3f.ZP.rotation(headRollAngle));
      poseStack.mulPose(Vector3f.YP.rotationDegrees(lerpYRot));
      poseStack.mulPose(Vector3f.XP.rotationDegrees(lerpXRot));
      Vector3f sleepVec = new Vector3f(baby ? 0.4F : 0.455F, 0.15F, baby ? 0.15F : 0.3F);
      Vector3f regularVec = new Vector3f(0.059F, 0.15F, -0.42F);
      if (sleeping) {
         poseStack.translate(sleepVec.x(), sleepVec.y(), sleepVec.z());
      } else {
         poseStack.translate(regularVec.x(), regularVec.y(), regularVec.z());
      }

      poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F)); // rotates the item forwards by 90 degrees
      if (sleeping) {
         poseStack.mulPose(Vector3f.ZP.rotationDegrees(45.0F)); // rotates the item leftwards by 45 degrees
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