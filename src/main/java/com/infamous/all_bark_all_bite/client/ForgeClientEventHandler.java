package com.infamous.all_bark_all_bite.client;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.registry.ABABItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = AllBarkAllBite.MODID, value = Dist.CLIENT)
public class ForgeClientEventHandler {
    @SubscribeEvent
    static void onInteraction(InputEvent.InteractionKeyMappingTriggered event){
        if(event.isUseItem()){
            LocalPlayer player = Minecraft.getInstance().player;
            if(player.getItemInHand(event.getHand()).is(ABABItems.WHISTLE.get())){
                if(player.getCooldowns().isOnCooldown(ABABItems.WHISTLE.get())){
                    event.setCanceled(true);
                    event.setSwingHand(false);
                }
            }
        }
    }

}
