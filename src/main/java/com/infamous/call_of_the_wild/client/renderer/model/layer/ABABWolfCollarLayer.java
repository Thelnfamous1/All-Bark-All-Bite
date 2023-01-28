package com.infamous.call_of_the_wild.client.renderer.model.layer;

import com.infamous.call_of_the_wild.client.renderer.model.ABABWolfModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

@SuppressWarnings("NullableProblems")
public class ABABWolfCollarLayer extends RenderLayer<Wolf, ABABWolfModel<Wolf>> {
   private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_collar.png");

   public ABABWolfCollarLayer(RenderLayerParent<Wolf, ABABWolfModel<Wolf>> p_117707_) {
      super(p_117707_);
   }

   public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Wolf wolf, float animPosDelta, float lerpAnimSpeed, float partialTicks, float bob, float lerpYRot, float lerpXRot) {
      if (wolf.isTame() && !wolf.isInvisible()) {
         float[] afloat = wolf.getCollarColor().getTextureDiffuseColors();
         renderColoredCutoutModel(this.getParentModel(), WOLF_COLLAR_LOCATION, poseStack, bufferSource, packedLight, wolf, afloat[0], afloat[1], afloat[2]);
      }
   }
}