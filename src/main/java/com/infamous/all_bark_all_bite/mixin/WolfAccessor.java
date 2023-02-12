package com.infamous.all_bark_all_bite.mixin;

import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Wolf.class)
public interface WolfAccessor {

    @Accessor
    boolean getIsShaking();
}
