package com.infamous.all_bark_all_bite.client.util;

import com.infamous.all_bark_all_bite.client.screen.WhistleScreen;
import com.infamous.all_bark_all_bite.common.item.AdjustableInstrumentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ABABClientUtil {
    public static void openWhistleScreen(InteractionHand hand) {
        //noinspection ConstantConditions
        ItemStack stack = Minecraft.getInstance().player.getItemInHand(hand);
        if(stack.getItem() instanceof AdjustableInstrumentItem adjustableInstrumentItem){
            Minecraft.getInstance().setScreen(new WhistleScreen(Minecraft.getInstance().player, stack, hand, adjustableInstrumentItem.getInstruments()));
        }
    }
}
