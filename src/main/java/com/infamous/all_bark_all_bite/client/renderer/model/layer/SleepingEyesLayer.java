package com.infamous.all_bark_all_bite.client.renderer.model.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class SleepingEyesLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private final ResourceLocation location;

   public SleepingEyesLayer(RenderLayerParent<T, M> parent, ResourceLocation location) {
      super(parent);
      this.location = location;
   }

   @Override
   public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float animPosDelta, float lerpAnimSpeed, float partialTicks, float bob, float lerpYRot, float lerpXRot) {
      if (entity.isSleeping() && !entity.isInvisible()) {
         renderColoredCutoutModel(this.getParentModel(), this.getTextureLocation(entity), poseStack, bufferSource, packedLight, entity, 1.0F, 1.0F, 1.0F);
      }
   }

   @Override
   protected ResourceLocation getTextureLocation(T entity) {
      return this.location;
   }
}