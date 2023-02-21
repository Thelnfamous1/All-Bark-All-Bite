package com.infamous.all_bark_all_bite.client.renderer.model;// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.infamous.all_bark_all_bite.client.renderer.model.animation.WolfAnimation;
import com.infamous.all_bark_all_bite.common.entity.AnimationControllerAccessor;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAnimationController;
import com.infamous.all_bark_all_bite.mixin.WolfAccessor;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.animal.Wolf;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class ABABWolfModel<T extends Wolf> extends ColorableHierarchicalModel<T> implements HeadedModel {
	public static final String HEAD = "head";
	public static final String BODY = "body";
	public static final String UPPER_BODY = "upper_body";
	public static final String RIGHT_HIND_LEG = "right_hind_leg";
	public static final String LEFT_HIND_LEG = "left_hind_leg";
	public static final String RIGHT_FRONT_LEG = "right_front_leg";
	public static final String LEFT_FRONT_LEG = "left_front_leg";
	public static final String TAIL = "tail";
	private static final float IDLE_SLEEP_ANIMATION_SPEED = 0.5F;
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart upperBody;
	private final ModelPart rightHindLeg;
	private final ModelPart leftHindLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftFrontLeg;
	private final ModelPart tail;
	private float partialTicks;

	public ABABWolfModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild(HEAD);
		this.body = root.getChild(BODY);
		this.upperBody = this.body.getChild(UPPER_BODY);
		this.rightHindLeg = root.getChild(RIGHT_HIND_LEG);
		this.leftHindLeg = root.getChild(LEFT_HIND_LEG);
		this.rightFrontLeg = root.getChild(RIGHT_FRONT_LEG);
		this.leftFrontLeg = root.getChild(LEFT_FRONT_LEG);
		this.tail = root.getChild(TAIL);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition root = meshDefinition.getRoot();

		PartDefinition head = root.addOrReplaceChild(HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 14).addBox(1.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 14).addBox(-3.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 10).addBox(-1.5F, -0.02F, -6.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.5F, -6.0F));

		PartDefinition body = root.addOrReplaceChild(BODY, CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 2.0F));

		PartDefinition body_rotation_r1 = body.addOrReplaceChild("body_rotation_r1", CubeListBuilder.create().texOffs(18, 14).addBox(-3.0F, -2.0F, -0.5F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition upperBody = body.addOrReplaceChild(UPPER_BODY, CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -2.0F));

		PartDefinition mane_rotation_r1 = upperBody.addOrReplaceChild("mane_rotation_r1", CubeListBuilder.create().texOffs(21, 0).addBox(-4.0F, -8.5F, -3.0F, 8.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition leg1 = root.addOrReplaceChild(RIGHT_HIND_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 16.0F, 7.0F));

		PartDefinition leg2 = root.addOrReplaceChild(LEFT_HIND_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 16.0F, 7.0F));

		PartDefinition leg3 = root.addOrReplaceChild(RIGHT_FRONT_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 16.0F, -4.0F));

		PartDefinition leg4 = root.addOrReplaceChild(LEFT_FRONT_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 16.0F, -4.0F));

		PartDefinition tail = root.addOrReplaceChild(TAIL, CubeListBuilder.create().texOffs(9, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 10.0F));

		return LayerDefinition.create(meshDefinition, 64, 32);
	}

	@Override
	public void prepareMobModel(T wolf, float limbSwing, float limbSwingAmount, float partialTicks) {
		this.partialTicks = partialTicks;
	}

	@Override
	public void setupAnim(T wolf, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animateHeadLookTarget(netHeadYaw, headPitch);
		AnimationControllerAccessor<SharedWolfAnimationController> aca = AnimationControllerAccessor.cast(wolf);
		SharedWolfAnimationController animationController = aca.getAnimationController();
		this.animate(animationController.attackAnimationState, WolfAnimation.ATTACK, ageInTicks);
		this.animate(animationController.babyAnimationState, WolfAnimation.BABY, ageInTicks);
		this.animate(animationController.crouchAnimationState, WolfAnimation.CROUCH, ageInTicks);
		this.animate(animationController.idleAnimationState, WolfAnimation.IDLE, ageInTicks);
		this.animate(animationController.idleSitAnimationState, WolfAnimation.IDLE_SIT, ageInTicks);
		this.animate(animationController.idleSleepAnimationState, WolfAnimation.IDLE_SLEEP, ageInTicks, IDLE_SLEEP_ANIMATION_SPEED);
		this.animate(animationController.jumpAnimationState, WolfAnimation.JUMP, ageInTicks);
		this.animate(animationController.leapAnimationState, WolfAnimation.LEAP, ageInTicks);
		this.animate(animationController.sitAnimationState, WolfAnimation.SIT, ageInTicks);
		this.animate(animationController.sleepAnimationState, WolfAnimation.SLEEP, ageInTicks);
		this.animate(animationController.sprintAnimationState, WolfAnimation.SPRINT, ageInTicks);
		this.animate(animationController.walkAnimationState, WolfAnimation.WALK, ageInTicks);
		this.animateInterestAndShaking(wolf, this.partialTicks);
	}

	private void animateInterestAndShaking(T wolf, float partialTicks) {
		boolean interested = wolf.isInterested();
		boolean shaking = ((WolfAccessor)wolf).getIsShaking();
		if(interested || shaking){
			this.head.zRot = (interested ? wolf.getHeadRollAngle(partialTicks) : 0.0F) + (shaking ? wolf.getBodyRollAngle(partialTicks, 0.0F) : 0.0F);
		}
		if(shaking){
			this.upperBody.zRot = wolf.getBodyRollAngle(partialTicks, -0.08F);
			this.body.zRot = wolf.getBodyRollAngle(partialTicks, -0.16F);
			this.tail.zRot = wolf.getBodyRollAngle(partialTicks, -0.2F);
		}
	}

	private void animateHeadLookTarget(float yRot, float xRot) {
		this.head.xRot = xRot * ((float)Math.PI / 180F);
		this.head.yRot = yRot * ((float)Math.PI / 180F);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public ModelPart getHead() {
		return this.head;
	}
}