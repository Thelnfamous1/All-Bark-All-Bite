package com.infamous.all_bark_all_bite.common.logic.proxy;

import com.infamous.all_bark_all_bite.client.screen.WhistleScreen;
import com.infamous.all_bark_all_bite.common.item.AdjustableInstrumentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ClientProxy{

    public static void openItemGui(Player player, ItemStack stack, InteractionHand hand) {
        if(FMLEnvironment.dist.isClient() && stack.getItem() instanceof AdjustableInstrumentItem adjustableInstrumentItem){
            Minecraft.getInstance().setScreen(new WhistleScreen(player, stack, hand, adjustableInstrumentItem.getInstruments()));
        }
    }
}
