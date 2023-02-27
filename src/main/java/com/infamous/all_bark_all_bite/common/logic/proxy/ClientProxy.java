package com.infamous.all_bark_all_bite.common.logic.proxy;

import com.infamous.all_bark_all_bite.client.screen.WhistleScreen;
import com.infamous.all_bark_all_bite.common.item.AdjustableInstrumentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClientProxy extends CommonProxy{
    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    @Override
    public void openItemGui(Player player, ItemStack stack, InteractionHand hand) {
        if(player.level.isClientSide && stack.getItem() instanceof AdjustableInstrumentItem adjustableInstrumentItem){
            MINECRAFT.setScreen(new WhistleScreen(player, stack, hand, adjustableInstrumentItem.getInstruments()));
        }
    }
}
