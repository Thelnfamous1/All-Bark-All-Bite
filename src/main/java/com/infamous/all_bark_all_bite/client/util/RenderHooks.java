package com.infamous.all_bark_all_bite.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class RenderHooks {

    public static void setupWolfRenderRotations(LivingEntity wolf, PoseStack poseStack, float lerpYBodyRot, float partialTick){
        if (wolf.isFullyFrozen()) {
            lerpYBodyRot += (float)(Math.cos((double)wolf.tickCount * 3.25D) * Math.PI * (double)0.4F);
        }

        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - lerpYBodyRot));

        if (wolf.deathTime > 0) {
            float deathFlipProgress = ((float)wolf.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
            deathFlipProgress = Mth.sqrt(deathFlipProgress);
            if (deathFlipProgress > 1.0F) {
                deathFlipProgress = 1.0F;
            }

            poseStack.mulPose(Vector3f.ZP.rotationDegrees(deathFlipProgress * 90.0F));
        } else if (wolf.isAutoSpinAttack()) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F - wolf.getXRot()));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(((float)wolf.tickCount + partialTick) * -75.0F));
        } else if (!wolf.hasPose(Pose.SLEEPING) && LivingEntityRenderer.isEntityUpsideDown(wolf)) {
            poseStack.translate(0.0D, wolf.getBbHeight() + 0.1F, 0.0D);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        }
    }
}
