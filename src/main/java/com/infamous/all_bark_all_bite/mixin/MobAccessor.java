package com.infamous.all_bark_all_bite.mixin;

import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobAccessor {

    @Invoker
    InteractionResult callCheckAndHandleImportantInteractions(Player player, InteractionHand hand);

    @Invoker
    Vec3i callGetPickupReach();
}
