package com.infamous.all_bark_all_bite.common.network;

import com.infamous.all_bark_all_bite.client.screen.WhistleScreen;
import com.infamous.all_bark_all_bite.common.item.AdjustableInstrumentItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundOpenWhistleScreenPacket {

    private final InteractionHand hand;

    public ClientboundOpenWhistleScreenPacket(InteractionHand hand) {
        this.hand = hand;
    }

    public ClientboundOpenWhistleScreenPacket(FriendlyByteBuf byteBuf) {
        this.hand = byteBuf.readEnum(InteractionHand.class);
    }

    public void write(FriendlyByteBuf byteBuf) {
        byteBuf.writeEnum(this.hand);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //noinspection ConstantConditions
            ItemStack stack = Minecraft.getInstance().player.getItemInHand(this.hand);
            if(stack.getItem() instanceof AdjustableInstrumentItem adjustableInstrumentItem){
                Minecraft.getInstance().setScreen(new WhistleScreen(Minecraft.getInstance().player, stack, this.hand, adjustableInstrumentItem.getInstruments()));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
