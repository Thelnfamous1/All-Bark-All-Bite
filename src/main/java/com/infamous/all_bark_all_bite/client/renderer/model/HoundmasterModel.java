package com.infamous.all_bark_all_bite.client.renderer.model;// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.infamous.all_bark_all_bite.common.entity.houndmaster.Houndmaster;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class HoundmasterModel<T extends Houndmaster> extends HierarchicalModel<T> implements ArmedModel, HeadedModel {
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart whistle;

	public HoundmasterModel(ModelPart root) {
		this.root = root;
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.leftArm = root.getChild("left_arm");
		this.whistle = this.leftArm.getChild("whistle");
		this.whistle.visible = false;
		this.rightArm = root.getChild("right_arm");
		this.leftLeg = root.getChild("left_leg");
		this.rightLeg = root.getChild("right_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition root = meshDefinition.getRoot();

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 21).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.5F, 0.0F));

		PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 63).addBox(-4.0F, -3.25F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.5F))
				.texOffs(24, 63).addBox(-3.0F, -3.25F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -8.5F, 0.0F));

		PartDefinition head_r1 = hat.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(48, 55).addBox(0.0005F, -6.7386F, -1.6254F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -0.0387F, 4.5468F, -1.1352F, 0.1739F, -0.0636F));

		PartDefinition head_r2 = hat.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(48, 55).addBox(0.0F, -6.6438F, -1.388F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, -0.2887F, 4.5468F, -0.8294F, -0.0764F, 0.0607F));

		PartDefinition head_r3 = hat.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(48, 55).addBox(-0.0012F, -6.64F, -1.412F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.2887F, 4.5468F, -1.1318F, -0.1996F, 0.066F));

		PartDefinition head_r4 = hat.addOrReplaceChild("head_r4", CubeListBuilder.create().texOffs(48, 55).addBox(0.0F, -6.7495F, -1.6145F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -0.0387F, 4.5468F, -0.8287F, 0.058F, -0.0211F));

		PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition beard = head.addOrReplaceChild("beard", CubeListBuilder.create().texOffs(30, 0).addBox(-3.5F, -2.0F, -0.75F, 7.0F, 5.0F, 1.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 0.0F, -4.0F));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 33).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(48, 33).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)).mirror(false), PartPose.offset(5.5F, -0.75F, 0.0F));

		PartDefinition left_arm_r1 = leftArm.addOrReplaceChild("left_arm_r1", CubeListBuilder.create().texOffs(48, 17).mirror().addBox(-2.0F, -2.05F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offsetAndRotation(1.0F, 8.3F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition whistle = leftArm.addOrReplaceChild("whistle", CubeListBuilder.create().texOffs(33, 80).addBox(-1.5F, -2.0F, -3.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(18, 80).addBox(-1.5F, -2.0F, -1.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 9.0F, -2.0F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 33).mirror().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(48, 33).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)), PartPose.offset(-5.5F, -0.75F, 0.0F));

		PartDefinition right_arm_r1 = rightArm.addOrReplaceChild("right_arm_r1", CubeListBuilder.create().texOffs(48, 17).addBox(-2.0F, -2.05F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-1.0F, 8.3F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -14.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 39).addBox(-4.0F, -14.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.4F))
				.texOffs(32, 49).addBox(-4.0F, -13.75F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(1.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(32, 17).addBox(-2.5F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 12.0F, 0.0F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 17).mirror().addBox(-1.5F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.5F, 12.0F, 0.0F));

		return LayerDefinition.create(meshDefinition, 64, 128);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks) {
		this.whistle.visible = entity.isWhistling();
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.animateHeadLookTarget(netHeadYaw, headPitch);
		this.animateLimbs(limbSwing, limbSwingAmount);
		AbstractIllager.IllagerArmPose armPose = entity.getArmPose();

		if (armPose == AbstractIllager.IllagerArmPose.ATTACKING) {
			if (entity.getMainHandItem().isEmpty()) {
				AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, ageInTicks);
			} else {
				AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, entity, this.attackTime, ageInTicks);
			}
		} else if (armPose == AbstractIllager.IllagerArmPose.SPELLCASTING) {
			this.rightArm.z = 0.0F;
			this.rightArm.x = -5.0F;
			this.leftArm.z = 0.0F;
			this.leftArm.x = 5.0F;
			this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
			this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
			this.rightArm.zRot = 2.3561945F;
			this.leftArm.zRot = -2.3561945F;
			this.rightArm.yRot = 0.0F;
			this.leftArm.yRot = 0.0F;
		} else if (armPose == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
			this.rightArm.yRot = -0.1F + this.head.yRot;
			this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
			this.leftArm.xRot = -0.9424779F + this.head.xRot;
			this.leftArm.yRot = this.head.yRot - 0.4F;
			this.leftArm.zRot = ((float)Math.PI / 2F);
		} else if (armPose == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
			AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
		} else if (armPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE) {
			AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, entity, true);
		} else if (armPose == AbstractIllager.IllagerArmPose.CELEBRATING) {
			this.rightArm.z = 0.0F;
			this.rightArm.x = -5.0F;
			this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
			this.rightArm.zRot = 2.670354F;
			this.rightArm.yRot = 0.0F;
			this.leftArm.z = 0.0F;
			this.leftArm.x = 5.0F;
			this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
			this.leftArm.zRot = -2.3561945F;
			this.leftArm.yRot = 0.0F;
		} else if(entity.isWhistling()){
			this.leftArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
			this.leftArm.yRot = this.head.yRot - ((float)Math.PI / 6F);
		}
	}

	private void animateHeadLookTarget(float yRot, float xRot) {
		this.head.xRot = xRot * ((float)Math.PI / 180F);
		this.head.yRot = yRot * ((float)Math.PI / 180F);
	}

	private void animateLimbs(float limbSwing, float limbSwingAmount){
		if (this.riding) {
			this.rightArm.xRot = (-(float)Math.PI / 5F);
			this.rightArm.yRot = 0.0F;
			this.rightArm.zRot = 0.0F;
			this.leftArm.xRot = (-(float)Math.PI / 5F);
			this.leftArm.yRot = 0.0F;
			this.leftArm.zRot = 0.0F;
			this.rightLeg.xRot = -1.4137167F;
			this.rightLeg.yRot = ((float)Math.PI / 10F);
			this.rightLeg.zRot = 0.07853982F;
			this.leftLeg.xRot = -1.4137167F;
			this.leftLeg.yRot = (-(float)Math.PI / 10F);
			this.leftLeg.zRot = -0.07853982F;
		} else {
			this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
			this.rightArm.yRot = 0.0F;
			this.rightArm.zRot = 0.0F;
			this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
			this.leftArm.yRot = 0.0F;
			this.leftArm.zRot = 0.0F;
			this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
			this.rightLeg.yRot = 0.0F;
			this.rightLeg.zRot = 0.0F;
			this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount * 0.5F;
			this.leftLeg.yRot = 0.0F;
			this.leftLeg.zRot = 0.0F;
		}
	}

	@Override
	public void translateToHand(HumanoidArm humanoidArm, PoseStack poseStack) {
		this.getArm(humanoidArm).translateAndRotate(poseStack);
	}

	private ModelPart getArm(HumanoidArm humanoidArm) {
		return humanoidArm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
	}

	@Override
	public ModelPart getHead() {
		return this.head;
	}
}