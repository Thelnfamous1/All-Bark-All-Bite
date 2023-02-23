package com.infamous.all_bark_all_bite.client.renderer;

import com.infamous.all_bark_all_bite.client.ABABModelLayers;
import com.infamous.all_bark_all_bite.client.renderer.model.ABABWolfModel;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.RWWolfArmorLayer;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.SharedWolfCollarLayer;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.SharedWolfHeldItemLayer;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfAi;
import com.infamous.all_bark_all_bite.common.util.CompatUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Wolf;

public class ABABWolfRenderer extends MobRenderer<Wolf, ABABWolfModel<Wolf>> {
   private static final float DEFAULT_SHADOW_RADIUS = 0.5F;
   private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
   private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
   private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

   public ABABWolfRenderer(EntityRendererProvider.Context context) {
      super(context, new ABABWolfModel<>(context.bakeLayer(ABABModelLayers.WOLF)), DEFAULT_SHADOW_RADIUS);
      this.addLayer(new SharedWolfCollarLayer<>(this));
      this.addLayer(new SharedWolfHeldItemLayer<>(this, context.getItemInHandRenderer()));
      if(CompatUtil.isRevampedWolfLoaded()){
         this.addLayer(new RWWolfArmorLayer<>(this, new ABABWolfModel<>(context.getModelSet().bakeLayer(ABABModelLayers.RW_WOLF_ARMOR))));
      }
   }

   @Override
   public void render(Wolf wolf, float lerpYRot, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      if (wolf.isWet()) {
         float wetShade = wolf.getWetShade(partialTicks);
         this.model.setColor(wetShade, wetShade, wetShade);
      }

      super.render(wolf, lerpYRot, partialTicks, poseStack, bufferSource, packedLight);
      if (wolf.isWet()) {
         this.model.setColor(1.0F, 1.0F, 1.0F);
      }
   }

   @Override
   protected void setupRotations(Wolf wolf, PoseStack poseStack, float ageInTicks, float lerpYBodyRot, float partialTick) {
      if (this.isShaking(wolf)) {
         lerpYBodyRot += (float)(Math.cos((double)wolf.tickCount * 3.25D) * Math.PI * (double)0.4F);
      }

      poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - lerpYBodyRot));

      if (wolf.deathTime > 0) {
         float deathFlipProgress = ((float)wolf.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
         deathFlipProgress = Mth.sqrt(deathFlipProgress);
         if (deathFlipProgress > 1.0F) {
            deathFlipProgress = 1.0F;
         }

         poseStack.mulPose(Vector3f.ZP.rotationDegrees(deathFlipProgress * this.getFlipDegrees(wolf)));
      } else if (wolf.isAutoSpinAttack()) {
         poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F - wolf.getXRot()));
         poseStack.mulPose(Vector3f.YP.rotationDegrees(((float)wolf.tickCount + partialTick) * -75.0F));
      } else if (!wolf.hasPose(Pose.SLEEPING) && isEntityUpsideDown(wolf)) {
         poseStack.translate(0.0D, wolf.getBbHeight() + 0.1F, 0.0D);
         poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
      }
   }

   @Override
   public ResourceLocation getTextureLocation(Wolf wolf) {
      if (wolf.isTame()) {
         return WOLF_TAME_LOCATION;
      } else {
         return wolf.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
      }
   }

   @Override
   protected void scale(Wolf wolf, PoseStack poseStack, float partialTick) {
      float scaleFactor = WolfAi.WOLF_SIZE_SCALE;
      if (wolf.isBaby()) {
         scaleFactor *= 0.5F;
         this.shadowRadius = DEFAULT_SHADOW_RADIUS * 0.5F;
      } else {
         this.shadowRadius = DEFAULT_SHADOW_RADIUS;
      }

      poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
   }
}