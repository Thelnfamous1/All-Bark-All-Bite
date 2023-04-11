package com.infamous.all_bark_all_bite.client.renderer.model.layer;

import com.infamous.all_bark_all_bite.client.renderer.model.animation.DogAnimation;
import com.infamous.all_bark_all_bite.client.renderer.model.animation.WolfAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class ItemInMouthLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
   /**
    * See the position vector shift for the model's head passed in to {@link DogAnimation#BABY} and {@link WolfAnimation#BABY}.
    * <br>
    * Here, we need to "undo" that position shift using a negation of those values (the y-value is negated within {@link KeyframeAnimations#posVec(float, float, float)}).
    * <br>
    * This is meant to correspond to baby head offset values normally passed into {@link AgeableListModel}.
    */
   private static final Vector3f BABY_HEAD_OFFSET = new Vector3f(0.0F, 1.0F, 2.0F);
   /**
    * Manually shift the item render position to the relative location of the parent model's mouth.
    * <br>
    * These are the same values BaguChan used for their "WolfHeldItemLayer", multiplied by 16.0F.
    */
   private static final Vector3f MOUTH_OFFSET = new Vector3f(0.944F, 2.4F, -6.72F);
   /**
    * The default baby head scale value normally passed into {@link AgeableListModel}.
    */
   private static final float BABY_HEAD_SCALE = 2.0F;
   private final ItemInHandRenderer itemInHandRenderer;

   public ItemInMouthLayer(RenderLayerParent<T, M> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
      super(renderLayerParent);
      this.itemInHandRenderer = itemInHandRenderer;
   }

   @Override
   public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float animPosDelta, float lerpAnimSpeed, float partialTicks, float bob, float lerpYRot, float lerpXRot) {
      ItemStack mainHandStack = entity.getMainHandItem();
      if(!mainHandStack.isEmpty()){
         poseStack.pushPose();
         if (this.getParentModel().young) {
            if(this.shouldScaleHead()){
               float headScale = 1.5F / this.getBabyHeadScale();
               poseStack.scale(headScale, headScale, headScale);
            }
            Vector3f babyHeadOffset = this.getBabyHeadOffset();
            poseStack.translate(babyHeadOffset.x() / 16.0F, babyHeadOffset.y() / 16.0F, babyHeadOffset.z() / 16.0F);
         }

         this.renderMouthWithItem(poseStack, bufferSource, packedLight, entity, mainHandStack);
         poseStack.popPose();
      }
   }

   private void renderMouthWithItem(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, ItemStack mainHandStack) {
      poseStack.pushPose();
      ModelPart head = (this.getParentModel()).getHead();
      head.translateAndRotate(poseStack);
      Vector3f mouthOffset = this.getMouthOffset();
      poseStack.translate(mouthOffset.x() / 16.0F, mouthOffset.y() / 16.0F, mouthOffset.z() / 16.0F);

      poseStack.mulPose(Axis.XP.rotationDegrees(90.0F)); // rotates the item backwards by 90 degrees to make it horizontal
      this.itemInHandRenderer.renderItem(entity, mainHandStack, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight);
      poseStack.popPose();
   }

   /**
    * For Axolotls, Bees, Chickens, Cows, Foxes, Ocelots, Pigs, Sheep, Tadpoles and Wolves, the value passed into {@link AgeableListModel} is false.
    * <br>
    * For Goats, Hoglins, "Humanoids", Horses, Pandas, Polar Bears, and Turtles, the value passed into it is true.
    */
   protected boolean shouldScaleHead() {
      return false;
   }

   protected float getBabyHeadScale() {
      return BABY_HEAD_SCALE;
   }

   protected Vector3f getBabyHeadOffset() {
      return BABY_HEAD_OFFSET;
   }

   protected Vector3f getMouthOffset() {
      return MOUTH_OFFSET;
   }
}