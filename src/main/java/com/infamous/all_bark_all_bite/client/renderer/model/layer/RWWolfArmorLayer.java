package com.infamous.all_bark_all_bite.client.renderer.model.layer;

import baguchan.revampedwolf.api.IHasArmor;
import baguchan.revampedwolf.item.DyedWolfArmorItem;
import baguchan.revampedwolf.item.WolfArmorItem;
import com.infamous.all_bark_all_bite.client.renderer.model.ABABWolfModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;

public class RWWolfArmorLayer<T extends Wolf, M extends ABABWolfModel<T>> extends RenderLayer<T, M> {
	private final M model;

	public RWWolfArmorLayer(RenderLayerParent<T, M> parent, M model) {
		super(parent);
		this.model = model;
	}

	public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float neatHeadYaw, float headPitch) {
		if (wolf instanceof IHasArmor hasArmor) {
			ItemStack armor = hasArmor.getArmor();
			if (armor.getItem() instanceof WolfArmorItem wolfArmorItem) {
				this.getParentModel().copyPropertiesTo(this.model);
				this.model.prepareMobModel(wolf, limbSwing, limbSwingAmount, partialTicks);
				this.model.setupAnim(wolf, limbSwing, limbSwingAmount, ageInTicks, neatHeadYaw, headPitch);
				float red;
				float green;
				float blue;
				if (wolfArmorItem instanceof DyedWolfArmorItem dyedWolfArmorItem) {
					int color = dyedWolfArmorItem.getColor(armor);
					red = (float) (color >> 16 & 255) / 255.0F;
					green = (float) (color >> 8 & 255) / 255.0F;
					blue = (float) (color & 255) / 255.0F;
				} else {
					red = 1.0F;
					green = 1.0F;
					blue = 1.0F;
				}

				VertexConsumer armorFoilBuffer = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(wolfArmorItem.getTexture()), false, armor.hasFoil());
				this.model.renderToBuffer(poseStack, armorFoilBuffer, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
			}
		}
	}
}