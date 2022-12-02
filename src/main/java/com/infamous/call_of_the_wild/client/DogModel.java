package com.infamous.call_of_the_wild.client;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import net.minecraft.client.model.ColorableAgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class DogModel<T extends Dog> extends ColorableAgeableListModel<T> {
   private static final String REAL_HEAD = "real_head";
   private static final String UPPER_BODY = "upper_body";
   private static final String REAL_TAIL = "real_tail";
   private final ModelPart head;
   private final ModelPart realHead;
   private final ModelPart body;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart tail;
   private final ModelPart realTail;
   private final ModelPart upperBody;
   private static final int LEG_SIZE = 8;

   public DogModel(ModelPart modelPart) {
      this.head = modelPart.getChild("head");
      this.realHead = this.head.getChild(REAL_HEAD);
      this.body = modelPart.getChild("body");
      this.upperBody = modelPart.getChild(UPPER_BODY);
      this.rightHindLeg = modelPart.getChild("right_hind_leg");
      this.leftHindLeg = modelPart.getChild("left_hind_leg");
      this.rightFrontLeg = modelPart.getChild("right_front_leg");
      this.leftFrontLeg = modelPart.getChild("left_front_leg");
      this.tail = modelPart.getChild("tail");
      this.realTail = this.tail.getChild(REAL_TAIL);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshDefinition = new MeshDefinition();
      PartDefinition root = meshDefinition.getRoot();

      // HEAD
      float headY = 13.5F;
      PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0F, headY, -7.0F));
      head.addOrReplaceChild(REAL_HEAD, CubeListBuilder.create()
                      .texOffs(0, 0)
                      .addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F)
                      .texOffs(16, 14)
                      .addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F)
                      .texOffs(16, 14)
                      .addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F)
                      .texOffs(0, 10)
                      .addBox(-0.5F, -0.001F, -5.0F, 3.0F, 3.0F, 4.0F),
              PartPose.ZERO);

      // BODY
      root.addOrReplaceChild("body", CubeListBuilder.create()
              .texOffs(18, 14)
              .addBox(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F),
              PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      root.addOrReplaceChild(UPPER_BODY, CubeListBuilder.create()
              .texOffs(21, 0)
              .addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F),
              PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create()
              .texOffs(0, 18)
              .addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F);

      // LEGS
      root.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, 7.0F));
      root.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, 7.0F));
      root.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, -4.0F));
      root.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, -4.0F));

      // TAIL
      PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create(),
              PartPose.offsetAndRotation(-1.0F, 12.0F, 8.0F, ((float)Math.PI / 5F), 0.0F, 0.0F));
      tail.addOrReplaceChild(REAL_TAIL, CubeListBuilder.create()
              .texOffs(9, 18)
              .addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F),
              PartPose.ZERO);

      return LayerDefinition.create(meshDefinition, 64, 32);
   }

   @Override
   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.head);
   }

   @Override
   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.tail, this.upperBody);
   }

   @Override
   public void prepareMobModel(T dog, float lerpAnimPos, float lerpAnimSpeed, float partialTicks) {
      if (dog.isAggressive()) {
         this.tail.yRot = 0.0F;
      } else {
         this.tail.yRot = Mth.cos(lerpAnimPos * 0.6662F) * 1.4F * lerpAnimSpeed;
      }

      if (dog.isInSittingPose()) {
         this.upperBody.setPos(-1.0F, 16.0F, -3.0F);
         this.upperBody.xRot = 1.2566371F;
         this.upperBody.yRot = 0.0F;
         this.body.setPos(0.0F, 18.0F, 0.0F);
         this.body.xRot = ((float)Math.PI / 4F);
         this.tail.setPos(-1.0F, 21.0F, 6.0F);
         this.rightHindLeg.setPos(-2.5F, 22.7F, 2.0F);
         this.rightHindLeg.xRot = ((float)Math.PI * 1.5F);
         this.leftHindLeg.setPos(0.5F, 22.7F, 2.0F);
         this.leftHindLeg.xRot = ((float)Math.PI * 1.5F);
         this.rightFrontLeg.xRot = 5.811947F;
         this.rightFrontLeg.setPos(-2.49F, 17.0F, -4.0F);
         this.leftFrontLeg.xRot = 5.811947F;
         this.leftFrontLeg.setPos(0.51F, 17.0F, -4.0F);
      } else {
         this.body.setPos(0.0F, 14.0F, 2.0F);
         this.body.xRot = ((float)Math.PI / 2F);
         this.upperBody.setPos(-1.0F, 14.0F, -3.0F);
         this.upperBody.xRot = this.body.xRot;
         this.tail.setPos(-1.0F, 12.0F, 8.0F);
         this.rightHindLeg.setPos(-2.5F, 16.0F, 7.0F);
         this.leftHindLeg.setPos(0.5F, 16.0F, 7.0F);
         this.rightFrontLeg.setPos(-2.5F, 16.0F, -4.0F);
         this.leftFrontLeg.setPos(0.5F, 16.0F, -4.0F);
         this.rightHindLeg.xRot = Mth.cos(lerpAnimPos * 0.6662F) * 1.4F * lerpAnimSpeed;
         this.leftHindLeg.xRot = Mth.cos(lerpAnimPos * 0.6662F + (float)Math.PI) * 1.4F * lerpAnimSpeed;
         this.rightFrontLeg.xRot = Mth.cos(lerpAnimPos * 0.6662F + (float)Math.PI) * 1.4F * lerpAnimSpeed;
         this.leftFrontLeg.xRot = Mth.cos(lerpAnimPos * 0.6662F) * 1.4F * lerpAnimSpeed;
      }

      this.realHead.zRot = dog.getHeadRollAngle(partialTicks) + dog.getBodyRollAngle(partialTicks, 0.0F);
      this.upperBody.zRot = dog.getBodyRollAngle(partialTicks, -0.08F);
      this.body.zRot = dog.getBodyRollAngle(partialTicks, -0.16F);
      this.realTail.zRot = dog.getBodyRollAngle(partialTicks, -0.2F);
   }

   @Override
   public void setupAnim(T dog, float lerpAnimPos, float lerpAnimSpeed, float bob, float lerpYRot, float lerpXRot) {
      this.head.xRot = lerpXRot * ((float)Math.PI / 180F);
      this.head.yRot = lerpYRot * ((float)Math.PI / 180F);
      this.tail.xRot = bob;
   }
}