package com.infamous.all_bark_all_bite.common.network;

import com.infamous.all_bark_all_bite.common.item.PetWhistleItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ServerboundUnbindPetWhistlePacket extends ServerboundUpdateGuiItemPacket {

    public ServerboundUnbindPetWhistlePacket(int slot){
        super(slot);
    }

    public static ServerboundUnbindPetWhistlePacket decoder(FriendlyByteBuf buffer) {
        return new ServerboundUnbindPetWhistlePacket(buffer.readInt());
    }

    @Override
    protected void updateGuiItem(ServerPlayer sender, ItemStack itemInSlot) {
        if (itemInSlot.getItem() instanceof PetWhistleItem) {
            PetWhistleItem.unbind(itemInSlot);
        }
    }
}
