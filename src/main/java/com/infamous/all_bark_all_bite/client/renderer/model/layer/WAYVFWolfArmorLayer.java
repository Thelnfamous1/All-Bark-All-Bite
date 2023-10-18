package com.infamous.all_bark_all_bite.client.renderer.model.layer;

import com.alexander.whatareyouvotingfor.capabilities.WolfArmourProvider;
import com.infamous.all_bark_all_bite.common.compat.CompatUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

public class WAYVFWolfArmorLayer<T extends Wolf, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final M model;

    public WAYVFWolfArmorLayer(RenderLayerParent<T, M> parent, M model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float neatHeadYaw, float headPitch) {
        wolf.getCapability(WolfArmourProvider.WOLF_ARMOUR).ifPresent((data) -> {
            if (data.hasArmour()) {
                this.getParentModel().copyPropertiesTo(this.model);
                this.model.prepareMobModel(wolf, limbSwing, limbSwingAmount, partialTicks);
                this.model.setupAnim(wolf, limbSwing, limbSwingAmount, ageInTicks, neatHeadYaw, headPitch);
                VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation(CompatUtil.WAYVF_MODID, "textures/entity/wolf_armour.png")));
                this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            }

        });
    }
}