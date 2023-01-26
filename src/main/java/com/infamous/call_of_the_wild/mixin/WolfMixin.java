package com.infamous.call_of_the_wild.mixin;

import com.infamous.call_of_the_wild.common.entity.AnimalAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin extends TamableAnimal implements AnimalAccessor {
    protected WolfMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "setTame", at = @At("HEAD"), cancellable = true)
    private void handleSetTame(boolean tame, CallbackInfo ci){
        super.setTame(tame);
        this.setHealth((float) this.getAttributeBaseValue(Attributes.MAX_HEALTH));
        ci.cancel(); // don't change the wolf's health and attack damage
    }

    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    private void handleCanMate(Animal partner, CallbackInfoReturnable<Boolean> cir){
        if (partner != this) {
            cir.setReturnValue(partner instanceof Wolf mate
                    && !this.isInSittingPose() && !mate.isInSittingPose()
                    && this.isInLove() && mate.isInLove());
        }
    }

    @Override
    public InteractionResult animalInteract(Player player, InteractionHand hand) {
        return super.mobInteract(player, hand);
    }

    @Override
    public void takeItemFromPlayer(Player player, InteractionHand hand, ItemStack itemStack) {
        this.usePlayerItem(player, hand, itemStack);
    }
}
