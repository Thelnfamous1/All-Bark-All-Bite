package com.infamous.all_bark_all_bite.mixin;

import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSelector;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(VibrationSystem.Data.class)
public interface VibrationDataAccessor {

    @Invoker("<init>")
    static VibrationSystem.Data newVibrationData(@Nullable VibrationInfo currentVibration, VibrationSelector selectionStrategy, int travelTimeInTicks, boolean reloadParticle){
        throw new AssertionError();
    }
}
