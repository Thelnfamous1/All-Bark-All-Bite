package com.infamous.call_of_the_wild.client.renderer.model.layer;

import com.infamous.call_of_the_wild.common.entity.CollaredMob;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;

@SuppressWarnings("NullableProblems")
public class SharedWolfCollarLayer<T extends TamableAnimal, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_collar.png");

   public SharedWolfCollarLayer(RenderLayerParent<T, M> renderLayerParent) {
      super(renderLayerParent);
   }

   @Override
   public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T tamableAnimal, float animPosDelta, float lerpAnimSpeed, float partialTicks, float bob, float lerpYRot, float lerpXRot) {
      if (tamableAnimal.isTame() && !tamableAnimal.isInvisible()) {
         this.renderCollar(poseStack, bufferSource, packedLight, tamableAnimal);
      }
   }

   protected void renderCollar(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T tamableAnimal) {
      float[] afloat = getCollarColor(tamableAnimal).getTextureDiffuseColors();
      renderColoredCutoutModel(this.getParentModel(), this.getCollarLocation(), poseStack, bufferSource, packedLight, tamableAnimal, afloat[0], afloat[1], afloat[2]);
   }

   private DyeColor getCollarColor(T tamableAnimal) {
      return tamableAnimal instanceof Wolf wolf ?
              wolf.getCollarColor() : tamableAnimal instanceof CollaredMob collaredMob ?
              collaredMob.getCollarColor() : DyeColor.RED;
   }

   public ResourceLocation getCollarLocation() {
      return WOLF_COLLAR_LOCATION;
   }
}