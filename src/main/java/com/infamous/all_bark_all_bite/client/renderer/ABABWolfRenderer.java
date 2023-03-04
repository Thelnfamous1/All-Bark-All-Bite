package com.infamous.all_bark_all_bite.client.renderer;

import com.infamous.all_bark_all_bite.client.ABABModelLayers;
import com.infamous.all_bark_all_bite.client.renderer.model.ABABWolfModel;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.SharedWolfCollarLayer;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.ItemInMouthLayer;
import com.infamous.all_bark_all_bite.client.renderer.model.layer.WolfSleepingLayer;
import com.infamous.all_bark_all_bite.client.compat.RWCompatClient;
import com.infamous.all_bark_all_bite.common.compat.CompatUtil;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

public class ABABWolfRenderer extends MobRenderer<Wolf, ABABWolfModel<Wolf>> {
   private static final float DEFAULT_SHADOW_RADIUS = 0.5F;
   private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
   private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
   private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

   public ABABWolfRenderer(EntityRendererProvider.Context context) {
      super(context, new ABABWolfModel<>(context.bakeLayer(ABABModelLayers.WOLF)), DEFAULT_SHADOW_RADIUS);
      this.addLayer(new WolfSleepingLayer(this));
      this.addLayer(new SharedWolfCollarLayer<>(this));
      this.addLayer(new ItemInMouthLayer<>(this, context.getItemInHandRenderer()));
      if(CompatUtil.isRWLoaded()){
         this.addLayer(RWCompatClient.getRWArmorLayer(this, context));
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
   public ResourceLocation getTextureLocation(Wolf wolf) {
      if(wolf.isSleeping()){
         return WOLF_LOCATION;
      } else if (wolf.isTame()) {
         return WOLF_TAME_LOCATION;
      } else {
         return wolf.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
      }
   }

   @Override
   protected void scale(Wolf wolf, PoseStack poseStack, float partialTick) {
      float scaleFactor = ABABConfig.wolfRenderSizeScale.get().floatValue() * ABABConfig.wolfHitboxSizeScale.get().floatValue();
      if (wolf.isBaby()) {
         scaleFactor *= 0.5F;
         this.shadowRadius = DEFAULT_SHADOW_RADIUS * 0.5F;
      } else {
         this.shadowRadius = DEFAULT_SHADOW_RADIUS;
      }

      poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
   }

}