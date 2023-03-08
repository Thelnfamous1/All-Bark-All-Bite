package com.infamous.all_bark_all_bite.client.renderer.model;// Made with Blockbench 4.3.1
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.infamous.all_bark_all_bite.client.renderer.model.animation.DogAnimation;
import com.infamous.all_bark_all_bite.common.entity.AnimationControllerAccess;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAnimationController;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.mixin.WolfAccessor;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DogModel<T extends Dog> extends ColorableHierarchicalModel<T> implements HeadedModel {
	public static final String HEAD = "head";
	public static final String BODY = "body";
	public static final String UPPER_BODY = "upper_body";
	public static final String RIGHT_HIND_LEG = "right_hind_leg";
	public static final String LEFT_HIND_LEG = "left_hind_leg";
	public static final String RIGHT_FRONT_LEG = "right_front_leg";
	public static final String LEFT_FRONT_LEG = "left_front_leg";
	public static final String TAIL = "tail";
	private static final String DOG_TAG = "dog_tag";
	public static final float IDLE_SLEEP_ANIMATION_SPEED = 0.5F;
	private final ModelPart dogTag;
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

	public DogModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild(HEAD);
		this.body = root.getChild(BODY);
		this.upperBody = this.body.getChild(UPPER_BODY);
		this.dogTag = this.upperBody.getChild(DOG_TAG);
		this.dogTag.visible = false;
		this.rightHindLeg = root.getChild(RIGHT_HIND_LEG);
		this.leftHindLeg = root.getChild(LEFT_HIND_LEG);
		this.rightFrontLeg = root.getChild(RIGHT_FRONT_LEG);
		this.leftFrontLeg = root.getChild(LEFT_FRONT_LEG);
		this.tail = root.getChild(TAIL);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition root = meshDefinition.getRoot();

		PartDefinition head = root.addOrReplaceChild(HEAD, CubeListBuilder.create().texOffs(29, 0).addBox(-2.0F, -3.0F, -3.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(32, 33).addBox(-IDLE_SLEEP_ANIMATION_SPEED, -0.02F, -6.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 12.0F, -4.5F));

		PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(24, 18).addBox(-2.964F, -3.005F, -0.8733F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.2285F, -0.1606F, -0.3867F));

		PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(24, 18).mirror().addBox(0.0F, -3.0F, -1.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -3.0F, 0.0F, 0.2285F, 0.1606F, 0.3867F));

		PartDefinition body = root.addOrReplaceChild(BODY, CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 2.5F));

		PartDefinition body_rotation_r1 = body.addOrReplaceChild("body_rotation_r1", CubeListBuilder.create().texOffs(32, 18).addBox(-4.0F, 0.0F, -6.5F, 8.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -IDLE_SLEEP_ANIMATION_SPEED, -6.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition body_rotation_r2 = body.addOrReplaceChild("body_rotation_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-4.0F, -5.0F, -4.5F, 8.0F, 13.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -IDLE_SLEEP_ANIMATION_SPEED, -1.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition body_rotation = body.addOrReplaceChild("body_rotation", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.0F, -IDLE_SLEEP_ANIMATION_SPEED, 1.5708F, 0.0F, 0.0F));

		PartDefinition mane = body.addOrReplaceChild(UPPER_BODY, CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition mane_rotation = mane.addOrReplaceChild("mane_rotation", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -7.0F, -8.0F, 9.0F, 7.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition tag = mane.addOrReplaceChild(DOG_TAG, CubeListBuilder.create().texOffs(40, 11).addBox(-1.0F, -7.75F, -3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition leg1 = root.addOrReplaceChild(RIGHT_HIND_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 19.0F, 7.0F));

		PartDefinition leg2 = root.addOrReplaceChild(LEFT_HIND_LEG, CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, 19.0F, 7.0F));

		PartDefinition leg3 = root.addOrReplaceChild(RIGHT_FRONT_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 19.0F, -2.0F));

		PartDefinition leg4 = root.addOrReplaceChild(LEFT_FRONT_LEG, CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, 19.0F, -2.0F));

		PartDefinition tail = root.addOrReplaceChild(TAIL, CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 10.0F));

		return LayerDefinition.create(meshDefinition, 51, 51);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void prepareMobModel(T dog, float animPos, float lerpAnimSpeed, float partialTicks) {
		this.partialTicks = partialTicks;
		this.dogTag.visible = dog.isTame() && dog.hasCustomName();
	}

	@Override
	public void setupAnim(T dog, float animPos, float lerpAnimSpeed, float bob, float headYRot, float headXRot) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animateHeadLookTarget(headYRot, headXRot);
		AnimationControllerAccess<SharedWolfAnimationController> aca = AnimationControllerAccess.cast(dog);
		SharedWolfAnimationController animationController = aca.getAnimationController();
		this.animate(animationController.attackAnimationState, DogAnimation.ATTACK, bob);
		this.animate(animationController.babyAnimationState, DogAnimation.BABY, bob);
		this.animate(animationController.crouchAnimationState, DogAnimation.CROUCH, bob);
		this.animate(animationController.digAnimationState, DogAnimation.DIG, bob);
		this.animate(animationController.idleAnimationState, DogAnimation.IDLE, bob);
		this.animate(animationController.idleDigAnimationState, DogAnimation.IDLE_DIG, bob);
		this.animate(animationController.idleSitAnimationState, DogAnimation.IDLE_SIT, bob);
		this.animate(animationController.idleSleepAnimationState, DogAnimation.IDLE_SLEEP, bob, IDLE_SLEEP_ANIMATION_SPEED);
		this.animate(animationController.jumpAnimationState, DogAnimation.JUMP, bob);
		this.animate(animationController.leapAnimationState, DogAnimation.LEAP, bob);
		this.animate(animationController.sitAnimationState, DogAnimation.SIT, bob);
		this.animate(animationController.sleepAnimationState, DogAnimation.SLEEP, bob);
		this.animate(animationController.sprintAnimationState, DogAnimation.SPRINT, bob);
		this.animate(animationController.walkAnimationState, DogAnimation.WALK, bob);
		this.animateInterestAndShaking(dog, this.partialTicks);
	}

	private void animateInterestAndShaking(T dog, float partialTicks1) {
		boolean interested = dog.isInterested();
		boolean shaking = ((WolfAccessor)dog).getIsShaking();
		if(interested || shaking){
			this.head.zRot = (interested ? dog.getHeadRollAngle(partialTicks1) : 0.0F) + (shaking ? dog.getBodyRollAngle(partialTicks1, 0.0F) : 0.0F);
		}
		if(shaking){
			this.upperBody.zRot = dog.getBodyRollAngle(partialTicks1, -0.08F);
			this.body.zRot = dog.getBodyRollAngle(partialTicks1, -0.16F);
			this.tail.zRot = dog.getBodyRollAngle(partialTicks1, -0.2F);
		}
	}

	private void animateHeadLookTarget(float yRot, float xRot) {
		this.head.xRot = xRot * ((float)Math.PI / 180F);
		this.head.yRot = yRot * ((float)Math.PI / 180F);
	}

	@Override
	public ModelPart getHead() {
		return this.head;
	}
}