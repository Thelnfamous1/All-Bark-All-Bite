package com.infamous.call_of_the_wild.mixin;

import com.infamous.call_of_the_wild.common.entity.wolf.WolfAi;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin extends TamableAnimal {
    protected WolfMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "getAmbientSound", at = @At("RETURN"), cancellable = true)
    private void handleGetAmbientSound(CallbackInfoReturnable<SoundEvent> cir){
        cir.setReturnValue(this.level.isClientSide ? null : WolfAi.getSoundForCurrentActivity(this.cast()).orElse(null));
    }

    private Wolf cast(){
        return (Wolf) (Object) this;
    }
}
