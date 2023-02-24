package com.infamous.all_bark_all_bite.mixin;

import com.github.alexthe668.domesticationinnovation.server.enchantment.DIEnchantmentRegistry;
import com.github.alexthe668.domesticationinnovation.server.entity.TameableUtils;
import com.infamous.all_bark_all_bite.common.util.CompatUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Swim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Swim.class)
public class SwimMixin {

    @Inject(
            at = @At("HEAD"),
            method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;)Z",
            cancellable = true
    )
    private void handleCheckExtraStartConditions(ServerLevel level, Mob mob, CallbackInfoReturnable<Boolean> cir) {
        if(CompatUtil.isDILoaded()){
            if (TameableUtils.isTamed(mob) && TameableUtils.hasEnchant(mob, DIEnchantmentRegistry.AMPHIBIOUS)) {
                cir.setReturnValue(false);
            }
        }
    }
}
