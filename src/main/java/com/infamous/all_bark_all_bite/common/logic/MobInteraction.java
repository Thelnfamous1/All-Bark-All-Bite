package com.infamous.all_bark_all_bite.common.logic;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface MobInteraction<T extends Mob>{

    InteractionResult apply(T mob, Player player, InteractionHand hand);
}
