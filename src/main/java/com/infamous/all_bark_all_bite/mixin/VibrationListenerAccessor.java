package com.infamous.all_bark_all_bite.mixin;

import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VibrationListener.class)
public interface VibrationListenerAccessor {

    @Accessor
    VibrationSelector getSelectionStrategy();
}
