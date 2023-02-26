package com.infamous.all_bark_all_bite.mixin;

import com.infamous.all_bark_all_bite.common.entity.*;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfHooks;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin extends TamableAnimal implements AnimalAccessor, AnimationControllerAccessor<SharedWolfAnimationController> {
    private SharedWolfAnimationController animationController;

    protected WolfMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void handleInit(EntityType<?> entityType, Level level, CallbackInfo ci){
        this.animationController = new SharedWolfAnimationController(this, TamableAnimal.DATA_FLAGS_ID, Entity.DATA_POSE);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if(this.animationController != null){
            this.animationController.onSyncedDataUpdatedAnimations(entityDataAccessor);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void handleTick(CallbackInfo ci){
        this.animationController.tickAnimations();
    }

    @Inject(method = "aiStep", at = @At("RETURN"))
    private void handleAiStep(CallbackInfo ci){
        this.animationController.aiStepAnimations();
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    private void handleDoHurtTarget(Entity target, CallbackInfoReturnable<Boolean> cir){
        this.level.broadcastEntityEvent(this, EntityAnimationController.ATTACKING_EVENT_ID);
    }

    @Inject(method = "setTame", at = @At("HEAD"), cancellable = true)
    private void handleSetTame(boolean tame, CallbackInfo ci){
        super.setTame(tame);
        ci.cancel(); // don't allow vanilla's changes to the wolf's health and attack damage upon tame
    }

    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    private void handleCanMate(Animal partner, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(WolfHooks.canWolfMate(this, partner));
    }

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
    private void handleGetAmbientSound(CallbackInfoReturnable<SoundEvent> cir){
        cir.setReturnValue(WolfHooks.getWolfAmbientSound((Wolf) ((Object) this)));
    }

    @Override
    public InteractionResult animalInteract(Player player, InteractionHand hand) {
        return super.mobInteract(player, hand);
    }

    @Override
    public void takeItemFromPlayer(Player player, InteractionHand hand, ItemStack itemStack) {
        this.usePlayerItem(player, hand, itemStack);
    }

    @Override
    public SharedWolfAnimationController getAnimationController() {
        return this.animationController;
    }

}
