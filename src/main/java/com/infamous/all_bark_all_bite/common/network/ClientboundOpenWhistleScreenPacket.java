package com.infamous.all_bark_all_bite.common.network;

import com.infamous.all_bark_all_bite.client.util.ABABClientUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
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
            ABABClientUtil.openWhistleScreen(this.hand);
        });
        ctx.get().setPacketHandled(true);
    }

}
