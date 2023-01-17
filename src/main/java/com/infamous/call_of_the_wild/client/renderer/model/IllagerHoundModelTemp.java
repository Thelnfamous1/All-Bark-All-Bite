package com.infamous.call_of_the_wild.client.renderer.model;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.entity.illager_hound.IllagerHound;
import net.minecraft.client.model.ColorableAgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

@SuppressWarnings("NullableProblems")
public class IllagerHoundModelTemp<T extends IllagerHound> extends ColorableAgeableListModel<T> {
   private static final String REAL_HEAD = "real_head";
   private static final String UPPER_BODY = "upper_body";
   private static final String REAL_TAIL = "real_tail";
   private final ModelPart head;
   @SuppressWarnings("FieldCanBeLocal")
   private final ModelPart realHead;
   private final ModelPart body;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart tail;
   @SuppressWarnings("FieldCanBeLocal")
   private final ModelPart realTail;
   private final ModelPart upperBody;
   @SuppressWarnings("unused")
   private static final int LEG_SIZE = 8;

   public IllagerHoundModelTemp(ModelPart modelPart) {
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
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      float headYOffset = 13.5F;
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0F, headYOffset, -7.0F));
      partdefinition1.addOrReplaceChild(REAL_HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F).texOffs(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).texOffs(16, 14).addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).texOffs(0, 10).addBox(-0.5F, -0.001F, -5.0F, 3.0F, 3.0F, 4.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 14).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      partdefinition.addOrReplaceChild(UPPER_BODY, CubeListBuilder.create().texOffs(21, 0).addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F);
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, 7.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, 7.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, -4.0F));
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, -4.0F));
      PartDefinition partdefinition2 = partdefinition.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 12.0F, 8.0F, ((float)Math.PI / 5F), 0.0F, 0.0F));
      partdefinition2.addOrReplaceChild(REAL_TAIL, CubeListBuilder.create().texOffs(9, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.tail, this.upperBody);
   }

   public void prepareMobModel(T hound, float p_104133_, float p_104134_, float p_104135_) {
      this.tail.yRot = 0.0F;

      this.body.setPos(0.0F, 14.0F, 2.0F);
      this.body.xRot = ((float)Math.PI / 2F);
      this.upperBody.setPos(-1.0F, 14.0F, -3.0F);
      this.upperBody.xRot = this.body.xRot;
      this.tail.setPos(-1.0F, 12.0F, 8.0F);
      this.rightHindLeg.setPos(-2.5F, 16.0F, 7.0F);
      this.leftHindLeg.setPos(0.5F, 16.0F, 7.0F);
      this.rightFrontLeg.setPos(-2.5F, 16.0F, -4.0F);
      this.leftFrontLeg.setPos(0.5F, 16.0F, -4.0F);
      this.rightHindLeg.xRot = Mth.cos(p_104133_ * 0.6662F) * 1.4F * p_104134_;
      this.leftHindLeg.xRot = Mth.cos(p_104133_ * 0.6662F + (float)Math.PI) * 1.4F * p_104134_;
      this.rightFrontLeg.xRot = Mth.cos(p_104133_ * 0.6662F + (float)Math.PI) * 1.4F * p_104134_;
      this.leftFrontLeg.xRot = Mth.cos(p_104133_ * 0.6662F) * 1.4F * p_104134_;
   }

   public void setupAnim(T hound, float p_104138_, float p_104139_, float p_104140_, float p_104141_, float p_104142_) {
      this.head.xRot = p_104142_ * ((float)Math.PI / 180F);
      this.head.yRot = p_104141_ * ((float)Math.PI / 180F);
      this.tail.xRot = hound.getTailAngle();
   }
}