package com.infamous.all_bark_all_bite.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

    @Shadow @Final public BrainDebugRenderer brainDebugRenderer;

    @Inject(method = "render", at = @At("RETURN"))
    private void doDebugRenderers(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo callbackInfo) {
        if(Minecraft.getInstance().player.isHolding(Items.DEBUG_STICK)){
            this.brainDebugRenderer.render(poseStack, bufferSource, camX, camY, camZ);
        }
    }
}
