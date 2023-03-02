package com.infamous.all_bark_all_bite.mixin;

import com.infamous.all_bark_all_bite.common.entity.wolf.WolfHooks;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    protected MobMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "wantsToPickUp", at = @At("HEAD"), cancellable = true)
    private void handleWantsToPickUp(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(this.getType() == EntityType.WOLF){
            cir.setReturnValue(WolfHooks.wolfWantsToPickUp(stack, (Mob) (Object) this));
        }
    }

    @Inject(method = "canPickUpLoot", at = @At("RETURN"), cancellable = true)
    private void handleCanPickUpLoot(CallbackInfoReturnable<Boolean> cir) {
        if(this.getType() == EntityType.WOLF){
            cir.setReturnValue(!GenericAi.isOnPickupCooldown(this));
        }
    }

    @Inject(method = "canTakeItem", at = @At("RETURN"), cancellable = true)
    private void handleCanTakeItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if(this.getType() == EntityType.WOLF){
            cir.setReturnValue(WolfHooks.canWolfTakeItem(stack, (Mob)(Object)this, cir.getReturnValue()));
        }
    }

    @Inject(method = "canHoldItem", at = @At("HEAD"), cancellable = true)
    private void handleCanHoldItem(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if(this.getType() == EntityType.WOLF && ((Mob)(Object)this) instanceof Animal animal){
            cir.setReturnValue(WolfHooks.canWolfHoldItem(itemStack, animal));
        }
    }

    @Inject(method = "pickUpItem", at = @At("HEAD"), cancellable = true)
    private void handlePickUpItem(ItemEntity itemEntity, CallbackInfo ci) {
        if(this.getType() == EntityType.WOLF){
            ci.cancel();
            WolfHooks.onWolfPickUpItem((Mob) (Object)this, itemEntity);
        }
    }
}
