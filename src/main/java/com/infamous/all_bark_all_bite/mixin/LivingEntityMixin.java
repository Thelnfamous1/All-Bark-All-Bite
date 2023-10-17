package com.infamous.all_bark_all_bite.mixin;

import com.infamous.all_bark_all_bite.common.entity.wolf.WolfHooks;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "brainProvider", at = @At("RETURN"), cancellable = true)
    private void handleBrainProvider(CallbackInfoReturnable<Brain.Provider<?>> cir){
        if(WolfHooks.canWolfChange(this.getType(), false, false)){
            cir.setReturnValue(WolfHooks.getWolfBrainProvider());
        }
    }

    @Inject(method = "getEatingSound", at = @At("HEAD"), cancellable = true)
    private void handleGetEatingSound(ItemStack stack, CallbackInfoReturnable<SoundEvent> cir) {
        if(WolfHooks.canWolfChange(this.getType(), false, false)){
            cir.setReturnValue(WolfHooks.getWolfEatingSound());
        }
    }
}
