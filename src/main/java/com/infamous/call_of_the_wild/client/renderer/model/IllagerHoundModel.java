package com.infamous.call_of_the_wild.client.renderer.model;

import com.infamous.call_of_the_wild.common.entity.illager_hound.IllagerHound;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

@SuppressWarnings("NullableProblems")
public class IllagerHoundModel<T extends IllagerHound> extends ColorableHierarchicalModel<T> {
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart upperBody;
   private final ModelPart body;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart tail;

   public IllagerHoundModel(ModelPart root) {
      this.root = root;
      this.body = root.getChild("body");
      this.upperBody = root.getChild("upper_body");
      this.head = this.upperBody.getChild("head");
      this.rightHindLeg = root.getChild("right_hind_leg");
      this.leftHindLeg = root.getChild("left_hind_leg");
      this.rightFrontLeg = root.getChild("right_front_leg");
      this.leftFrontLeg = root.getChild("left_front_leg");
      this.tail = root.getChild("tail");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();

      PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 0.0F));

      PartDefinition body_rotation = body.addOrReplaceChild("body_rotation", CubeListBuilder.create().texOffs(19, 22).mirror().addBox(0.0F, -5.75F, 6.0F, 0.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
              .texOffs(0, 0).addBox(-3.5F, -10.0F, -3.0F, 7.0F, 17.0F, 10.0F, new CubeDeformation(0.0F))
              .texOffs(0, 27).mirror().addBox(-3.5F, -2.0F, -6.0F, 7.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
              .texOffs(0, 27).addBox(-3.5F, -2.0F, -6.0F, 7.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

      PartDefinition body_rotation_r1 = body_rotation.addOrReplaceChild("body_rotation_r1", CubeListBuilder.create().texOffs(0, 27).addBox(-3.0F, -4.0F, -1.5F, 7.0F, 8.0F, 3.0F, new CubeDeformation(0.7F)), PartPose.offsetAndRotation(0.0F, -5.5F, -4.25F, 0.0F, 0.0F, -1.5708F));

      PartDefinition upperBody = partdefinition.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offset(-1.0F, 13.0F, -2.5F));

      PartDefinition upperBody_r1 = upperBody.addOrReplaceChild("upper_body_r1", CubeListBuilder.create().texOffs(20, 22).addBox(0.0F, -3.0F, -2.5F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -9.7479F, -4.5255F, 2.0071F, 0.0F, 0.0F));

      PartDefinition upperBody_r2 = upperBody.addOrReplaceChild("upper_body_r2", CubeListBuilder.create().texOffs(20, 26).addBox(0.0F, -1.0F, 1.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
              .texOffs(36, 32).addBox(-3.5F, 0.75F, -5.75F, 7.0F, 5.0F, 6.0F, new CubeDeformation(0.75F)), PartPose.offsetAndRotation(1.0F, -8.75F, -0.75F, 0.4363F, 0.0F, 0.0F));

      PartDefinition upperBody_r3 = upperBody.addOrReplaceChild("upper_body_r3", CubeListBuilder.create().texOffs(26, 43).addBox(-4.5F, -3.5F, -3.6F, 9.0F, 8.0F, 10.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(1.0F, 0.25F, -2.0F, 1.5708F, 0.0F, 0.0F));

      PartDefinition head = upperBody.addOrReplaceChild("head", CubeListBuilder.create().texOffs(34, 0).addBox(-2.0F, -4.0F, -4.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
              .texOffs(0, 48).addBox(-3.0F, -5.0F, -0.25F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
              .texOffs(34, 17).addBox(-0.5F, -1.02F, -7.25F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.75F, -6.75F));

      PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(42, 11).addBox(-1.964F, -6.005F, -0.3733F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
              .texOffs(34, 11).addBox(-2.964F, -4.005F, -0.8733F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -1.0F, 0.2285F, -0.1606F, -0.3867F));

      PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(42, 11).addBox(1.0F, -6.0F, -0.5F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
              .texOffs(34, 11).mirror().addBox(0.0F, -4.0F, -1.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -4.0F, -1.0F, 0.2285F, 0.1606F, 0.3867F));

      PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(34, 24).addBox(-1.0F, -0.55F, -3.25F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 1.23F, -3.5F, 0.4363F, 0.0F, 0.0F));

      PartDefinition rightHindLeg = partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(48, 11).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, 17.0F, 7.0F));

      PartDefinition leftHindLeg = partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 17.0F, 7.0F));

      PartDefinition rightFrontLeg = partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(24, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-2.75F, 16.75F, -5.5F));

      PartDefinition leftFrontLeg = partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 11).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offset(2.75F, 16.75F, -5.5F));

      PartDefinition tail = partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, 10.0F));

      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   @Override
   public ModelPart root() {
      return this.root;
   }

   @Override
   public void prepareMobModel(T hound, float limbSwing, float limbSwingAmount, float partialTicks) {
      if (hound.isAggressive()) {
         this.tail.yRot = 0.0F;
      } else {
         this.tail.yRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      }

      //this.body.setPos(0.0F, 14.0F, 2.0F);
      //this.body.xRot = ((float)Math.PI / 2F);
      //this.upperBody.setPos(-1.0F, 14.0F, -3.0F);
      //this.upperBody.xRot = this.body.xRot;
      //this.tail.setPos(-1.0F, 12.0F, 8.0F);
      //this.rightHindLeg.setPos(-2.5F, 16.0F, 7.0F);
      //this.leftHindLeg.setPos(0.5F, 16.0F, 7.0F);
      //this.rightFrontLeg.setPos(-2.5F, 16.0F, -4.0F);
      //this.leftFrontLeg.setPos(0.5F, 16.0F, -4.0F);
      this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
      this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
      this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
      this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

      this.head.zRot = this.getHeadRollAngle(partialTicks) + this.getBodyRollAngle(partialTicks, 0.0F);
      this.upperBody.zRot = this.getBodyRollAngle(partialTicks, -0.08F);
      this.body.zRot = this.getBodyRollAngle(partialTicks, -0.16F);
      this.tail.zRot = this.getBodyRollAngle(partialTicks, -0.2F);
   }

   public float getBodyRollAngle(float partialTick, float zRot) {
      float bodyRoll = (Mth.lerp(partialTick, 0, 0) + zRot) / 1.8F;
      if (bodyRoll < 0.0F) {
         bodyRoll = 0.0F;
      } else if (bodyRoll > 1.0F) {
         bodyRoll = 1.0F;
      }

      return Mth.sin(bodyRoll * (float)Math.PI) * Mth.sin(bodyRoll * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
   }

   public float getHeadRollAngle(float partialTick) {
      return Mth.lerp(partialTick, 0, 0) * 0.15F * (float)Math.PI;
   }

   @Override
   public void setupAnim(T hound, float p_104138_, float p_104139_, float p_104140_, float netHeadYaw, float headPitch) {
      this.animateHeadLookTarget(netHeadYaw, headPitch);
      this.tail.xRot = this.getTailAngle(hound);
   }

   private void animateHeadLookTarget(float yRot, float xRot) {
      this.head.xRot = xRot * ((float)Math.PI / 180F);
      this.head.yRot = yRot * ((float)Math.PI / 180F);
   }

   private float getTailAngle(T illagerHound) {
      if (illagerHound.isAggressive()) {
         return (float) (49F * Math.PI / 100F);
      } else {
         return ((float)Math.PI / 5F);
      }
   }
}