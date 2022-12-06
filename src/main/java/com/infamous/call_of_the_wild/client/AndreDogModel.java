package com.infamous.call_of_the_wild.client;// Made with Blockbench 4.3.1
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class AndreDogModel<T extends Dog> extends HierarchicalModel<T> {
	public static final String HEAD = "head";
	public static final String BODY = "body";
	public static final String UPPER_BODY = "upper_body";
	public static final String RIGHT_HIND_LEG = "right_hind_leg";
	public static final String LEFT_HIND_LEG = "left_hind_leg";
	public static final String RIGHT_FRONT_LEG = "right_front_leg";
	public static final String LEFT_FRONT_LEG = "left_front_leg";
	public static final String TAIL = "tail";
	private static final String DOG_TAG = "dog_tag";
	private final ModelPart dogTag;
	private float r = 1.0F;
	private float g = 1.0F;
	private float b = 1.0F;
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

	public AndreDogModel(ModelPart root) {
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

		PartDefinition head = root.addOrReplaceChild(HEAD, CubeListBuilder.create()
				.texOffs(29, 0).addBox(-2.0F, -3.0F, -3.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(32, 33).addBox(-0.5F, -0.02F, -6.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.0F, 12.0F, -4.5F));

		PartDefinition headR1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create()
				.texOffs(24, 18).addBox(-2.964F, -3.005F, -0.8733F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.2285F, -0.1606F, -0.3867F));

		PartDefinition headR2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create()
				.texOffs(24, 18).mirror().addBox(0.0F, -3.0F, -1.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(2.0F, -3.0F, 0.0F, 0.2285F, 0.1606F, 0.3867F));

		PartDefinition body = root.addOrReplaceChild(BODY, CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 2.5F));

		PartDefinition bodyRotationR1 = body.addOrReplaceChild("body_rotation_r1", CubeListBuilder.create()
				.texOffs(32, 18).addBox(-4.0F, 0.0F, -6.5F, 8.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -0.5F, -6.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition bodyRotationR2 = body.addOrReplaceChild("body_rotation_r2", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-4.0F, -5.0F, -4.5F, 8.0F, 13.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -0.5F, -1.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition bodyRotation = body.addOrReplaceChild("body_rotation", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, -1.0F, -0.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition upperBody = body.addOrReplaceChild(UPPER_BODY, CubeListBuilder.create(),
				PartPose.offset(-1.0F, -1.5F, -0.5F));

		PartDefinition upperBodyRotation = upperBody.addOrReplaceChild("upper_body_rotation", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0F, -3.5F, -5.5F, 9.0F, 7.0F, 11.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, 2.5F, -3.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition dogTag = upperBody.addOrReplaceChild(DOG_TAG, CubeListBuilder.create()
				.texOffs(40, 11).addBox(-1.5F, -4.25F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, 2.5F, -3.0F, 1.5708F, 0.0F, 0.0F));


		PartDefinition rightHindLeg = root.addOrReplaceChild(RIGHT_HIND_LEG, CubeListBuilder.create()
				.texOffs(0, 18).addBox(0.0F, 3.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 16.0F, 7.0F));

		PartDefinition leftHindLeg = root.addOrReplaceChild(LEFT_HIND_LEG, CubeListBuilder.create()
				.texOffs(0, 18).mirror().addBox(0.0F, 3.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(1.0F, 16.0F, 7.0F));

		PartDefinition rightFrontLeg = root.addOrReplaceChild(RIGHT_FRONT_LEG, CubeListBuilder.create()
				.texOffs(0, 18).addBox(0.0F, 3.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 16.0F, -2.0F));

		PartDefinition leftFrontLeg = root.addOrReplaceChild(LEFT_FRONT_LEG, CubeListBuilder.create()
				.texOffs(0, 18).mirror().addBox(0.0F, 3.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(1.0F, 16.0F, -2.0F));

		PartDefinition tail = root.addOrReplaceChild(TAIL, CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 10.0F));

		return LayerDefinition.create(meshDefinition, 51, 51);
	}

	public void setColor(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		super.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, this.r * red, this.g * green, this.b * blue, alpha);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void prepareMobModel(T dog, float animPos, float lerpAnimSpeed, float partialTicks) {
		this.root().getAllParts().forEach(ModelPart::resetPose); // this runs before setupAnim anyway, so best to do it here
		this.partialTicks = partialTicks;
		this.dogTag.visible = dog.isTame() && dog.hasCustomName();
	}

	@Override
	public void setupAnim(T dog, float animPos, float lerpAnimSpeed, float bob, float headYRot, float headXRot) {
		this.animateHeadLookTarget(headYRot, headXRot);
		this.animate(dog.babyAnimationState, DogAnimation.DOG_BABY_SCALING, bob);
		this.animate(dog.sitAnimationState, DogAnimation.DOG_SIT, bob);
		this.animate(dog.walkAnimationState, DogAnimation.DOG_WALK, bob);
		this.animate(dog.runAnimationState, DogAnimation.DOG_RUN, bob);
		this.animate(dog.jumpAnimationState, DogAnimation.DOG_JUMP, bob);
		this.animateInterest(dog, this.partialTicks);
		this.animateShaking(dog, this.partialTicks);
	}

	private void animateHeadLookTarget(float yRot, float xRot) {
		this.head.xRot = xRot * ((float)Math.PI / 180F);
		this.head.yRot = yRot * ((float)Math.PI / 180F);
	}

	private void animateInterest(T dog, float partialTicks) {
		this.head.zRot = dog.getHeadRollAngle(partialTicks) + dog.getBodyRollAngle(partialTicks, 0.0F);
	}

	private void animateShaking(T dog, float partialTicks) {
		this.upperBody.zRot = dog.getBodyRollAngle(partialTicks, -0.08F);
		this.body.zRot = dog.getBodyRollAngle(partialTicks, -0.16F);
		this.tail.zRot = dog.getBodyRollAngle(partialTicks, -0.2F);
	}
}