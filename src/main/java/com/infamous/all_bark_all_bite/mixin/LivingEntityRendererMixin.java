package com.infamous.all_bark_all_bite.mixin;

import com.infamous.all_bark_all_bite.client.util.RenderHooks;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfHooks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    @Inject(at = @At("HEAD"), method = "setupRotations", cancellable = true)
    private void handleSetupRotations(T entity, PoseStack poseStack, float ageInTicks, float lerpYBodyRot, float partialTick, CallbackInfo ci){
        if(WolfHooks.canWolfChange(entity.getType(), true, true)){
            ci.cancel();
            RenderHooks.setupWolfRenderRotations(entity, poseStack, lerpYBodyRot, partialTick);
        }
    }
}
