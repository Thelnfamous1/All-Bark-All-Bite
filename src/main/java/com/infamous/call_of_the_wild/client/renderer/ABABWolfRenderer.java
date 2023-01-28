package com.infamous.call_of_the_wild.client.renderer;

import com.infamous.call_of_the_wild.client.ABABModelLayers;
import com.infamous.call_of_the_wild.client.renderer.model.ABABWolfModel;
import com.infamous.call_of_the_wild.client.renderer.model.layer.ABABWolfCollarLayer;
import com.infamous.call_of_the_wild.common.entity.wolf.WolfAi;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

@SuppressWarnings("NullableProblems")
public class ABABWolfRenderer extends MobRenderer<Wolf, ABABWolfModel<Wolf>> {
   private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
   private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
   private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

   public ABABWolfRenderer(EntityRendererProvider.Context context) {
      super(context, new ABABWolfModel<>(context.bakeLayer(ABABModelLayers.WOLF)), 0.5F);
      this.addLayer(new ABABWolfCollarLayer(this));
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
      if (wolf.isTame()) {
         return WOLF_TAME_LOCATION;
      } else {
         return wolf.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
      }
   }

   @Override
   protected void scale(Wolf wolf, PoseStack poseStack, float partialTick) {
      poseStack.scale(WolfAi.WOLF_SIZE_SCALE, WolfAi.WOLF_SIZE_SCALE, WolfAi.WOLF_SIZE_SCALE);
   }
}