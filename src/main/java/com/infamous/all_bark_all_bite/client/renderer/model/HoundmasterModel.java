package com.infamous.all_bark_all_bite.client.renderer.model;// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.infamous.all_bark_all_bite.client.renderer.model.animation.HoundmasterAnimation;
import com.infamous.all_bark_all_bite.common.entity.houndmaster.Houndmaster;
import com.infamous.all_bark_all_bite.mixin.IllagerModelAccessor;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.monster.AbstractIllager;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class HoundmasterModel<T extends Houndmaster> extends IllagerModel<T> {
	public static final String BODY = "body";
	public static final String HEAD = "head";
	public static final String LEFT_ARM = "left_arm";
	public static final String WHISTLE = "whistle";
	public static final String RIGHT_ARM = "right_arm";
	public static final String LEFT_LEG = "left_leg";
	public static final String RIGHT_LEG = "right_leg";
	private final ModelPart whistle;

	public HoundmasterModel(ModelPart root) {
		super(root);
		this.getHat().visible = true;
		((IllagerModelAccessor)this).getArms().visible = false;
		this.whistle = root.getChild(LEFT_ARM).getChild(WHISTLE);
		this.whistle.visible = false;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition root = meshDefinition.getRoot();

		PartDefinition head = root.addOrReplaceChild(HEAD, CubeListBuilder.create().texOffs(0, 21).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.5F, 0.0F));

		PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 63).addBox(-4.0F, -3.25F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.5F))
				.texOffs(24, 63).addBox(-3.0F, -3.25F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -8.5F, 0.0F));

		PartDefinition head_r1 = hat.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(48, 55).addBox(0.0005F, -6.7386F, -1.6254F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -0.0387F, 4.5468F, -1.1352F, 0.1739F, -0.0636F));

		PartDefinition head_r2 = hat.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(48, 55).addBox(0.0F, -6.6438F, -1.388F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, -0.2887F, 4.5468F, -0.8294F, -0.0764F, 0.0607F));

		PartDefinition head_r3 = hat.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(48, 55).addBox(-0.0012F, -6.64F, -1.412F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.2887F, 4.5468F, -1.1318F, -0.1996F, 0.066F));

		PartDefinition head_r4 = hat.addOrReplaceChild("head_r4", CubeListBuilder.create().texOffs(48, 55).addBox(0.0F, -6.7495F, -1.6145F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -0.0387F, 4.5468F, -0.8287F, 0.058F, -0.0211F));

		PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition beard = head.addOrReplaceChild("beard", CubeListBuilder.create().texOffs(30, 0).addBox(-3.5F, -2.0F, -0.75F, 7.0F, 5.0F, 1.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 0.0F, -4.0F));

		PartDefinition leftArm = root.addOrReplaceChild(LEFT_ARM, CubeListBuilder.create().texOffs(32, 33).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(48, 33).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)).mirror(false), PartPose.offset(5.5F, -0.75F, 0.0F));

		PartDefinition left_arm_r1 = leftArm.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(48, 17).mirror().addBox(-2.0F, -2.05F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(1.0F, 8.3F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition whistle = leftArm.addOrReplaceChild(WHISTLE, CubeListBuilder.create().texOffs(33, 80).addBox(-1.5F, -2.0F, -3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(18, 80).addBox(-1.5F, -2.0F, -1.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 9.0F, -2.0F));

		PartDefinition rightArm = root.addOrReplaceChild(RIGHT_ARM, CubeListBuilder.create().texOffs(32, 33).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(48, 33).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)), PartPose.offset(-5.5F, -0.75F, 0.0F));

		PartDefinition right_arm_r1 = rightArm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(48, 17).addBox(-2.0F, -2.05F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-1.0F, 8.3F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition body = root.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -14.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 39).addBox(-4.0F, -14.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.4F))
				.texOffs(32, 49).addBox(-4.0F, -13.75F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(1.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition leftLeg = root.addOrReplaceChild(LEFT_LEG, CubeListBuilder.create().texOffs(32, 17).addBox(-2.5F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 12.0F, 0.0F));

		PartDefinition rightLeg = root.addOrReplaceChild(RIGHT_LEG, CubeListBuilder.create().texOffs(32, 17).mirror().addBox(-1.5F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.5F, 12.0F, 0.0F));

		// need this here so IllagerModel doesn't throw a NSEE
		PartDefinition arms = root.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F).texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));

		return LayerDefinition.create(meshDefinition, 64, 128);
	}

	@Override
	public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
		this.whistle.visible = entity.isWhistling();
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		if(this.riding || entity.getArmPose() != AbstractIllager.IllagerArmPose.NEUTRAL){
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		} else{
			this.animateLookTarget(netHeadYaw, headPitch);
			this.animate(entity.animationController.idleAnimationState, HoundmasterAnimation.HOUNDMASTER_IDLE, ageInTicks);
			this.animate(entity.animationController.walkAnimationState, HoundmasterAnimation.HOUNDMASTER_WALK, ageInTicks);
			this.animate(entity.animationController.whistleAnimationState, HoundmasterAnimation.HOUNDMASTER_SUMMON, ageInTicks);
		}
	}

	private void animateLookTarget(float netHeadYaw, float headPitch) {
		this.getHead().yRot = netHeadYaw * ((float)Math.PI / 180F);
		this.getHead().xRot = headPitch * ((float)Math.PI / 180F);
	}
}