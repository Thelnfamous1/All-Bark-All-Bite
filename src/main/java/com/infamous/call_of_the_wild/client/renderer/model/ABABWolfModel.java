package com.infamous.call_of_the_wild.client.renderer.model;// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.infamous.call_of_the_wild.client.renderer.model.animation.DogAnimation;
import com.infamous.call_of_the_wild.client.renderer.model.animation.WolfAnimation;
import com.infamous.call_of_the_wild.common.entity.AnimationControllerAccessor;
import com.infamous.call_of_the_wild.common.entity.SharedWolfAnimationController;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.animal.Wolf;

@SuppressWarnings({"NullableProblems", "FieldCanBeLocal", "unused"})
public class ABABWolfModel<T extends Wolf> extends ColorableHierarchicalModel<T> {
	public static final String HEAD = "head";
	public static final String BODY = "body";
	public static final String UPPER_BODY = "upper_body";
	public static final String RIGHT_HIND_LEG = "right_hind_leg";
	public static final String LEFT_HIND_LEG = "left_hind_leg";
	public static final String RIGHT_FRONT_LEG = "right_front_leg";
	public static final String LEFT_FRONT_LEG = "left_front_leg";
	public static final String TAIL = "tail";
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
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild(HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 14).addBox(1.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 14).addBox(-3.0F, -5.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 10).addBox(-1.5F, -0.02F, -6.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 13.5F, -6.0F));

		PartDefinition body = partdefinition.addOrReplaceChild(BODY, CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 2.0F));

		PartDefinition body_rotation_r1 = body.addOrReplaceChild("body_rotation_r1", CubeListBuilder.create().texOffs(18, 14).addBox(-3.0F, -2.0F, -0.5F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, 0.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition upperBody = body.addOrReplaceChild(UPPER_BODY, CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -2.0F));

		PartDefinition mane_rotation_r1 = upperBody.addOrReplaceChild("mane_rotation_r1", CubeListBuilder.create().texOffs(21, 0).addBox(-4.0F, -8.5F, -3.0F, 8.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition leg1 = partdefinition.addOrReplaceChild(RIGHT_HIND_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 16.0F, 7.0F));

		PartDefinition leg2 = partdefinition.addOrReplaceChild(LEFT_HIND_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 16.0F, 7.0F));

		PartDefinition leg3 = partdefinition.addOrReplaceChild(RIGHT_FRONT_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 16.0F, -4.0F));

		PartDefinition leg4 = partdefinition.addOrReplaceChild(LEFT_FRONT_LEG, CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 16.0F, -4.0F));

		PartDefinition tail = partdefinition.addOrReplaceChild(TAIL, CubeListBuilder.create().texOffs(9, 18).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 10.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
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
		this.animate(animationController.idleSleepAnimationState, WolfAnimation.IDLE_SLEEP, ageInTicks);
		this.animate(animationController.jumpAnimationState, WolfAnimation.JUMP, ageInTicks);
		this.animate(animationController.leapAnimationState, WolfAnimation.LEAP, ageInTicks);
		this.animate(animationController.shakeAnimationState, DogAnimation.SHAKE, ageInTicks);
		this.animate(animationController.sitAnimationState, WolfAnimation.SIT, ageInTicks);
		this.animate(animationController.sleepAnimationState, WolfAnimation.SLEEP, ageInTicks);
		this.animate(animationController.sprintAnimationState, WolfAnimation.SPRINT, ageInTicks);
		this.animate(animationController.walkAnimationState, WolfAnimation.WALK, ageInTicks);
		this.animateInterest(wolf, this.partialTicks);
	}

	private void animateHeadLookTarget(float yRot, float xRot) {
		this.head.xRot = xRot * ((float)Math.PI / 180F);
		this.head.yRot = yRot * ((float)Math.PI / 180F);
	}

	private void animateInterest(T wolf, float partialTicks) {
		this.head.zRot = wolf.getHeadRollAngle(partialTicks); //+ dog.getBodyRollAngle(partialTicks, 0.0F);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}