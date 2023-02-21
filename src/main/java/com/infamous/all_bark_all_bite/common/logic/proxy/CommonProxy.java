package com.infamous.all_bark_all_bite.common.logic.proxy;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class CommonProxy {

    public abstract void openItemGui(Player player, ItemStack stack, InteractionHand hand);
}
