package com.infamous.all_bark_all_bite.common.network;

import com.infamous.all_bark_all_bite.common.item.AdjustableInstrumentItem;
import com.infamous.all_bark_all_bite.common.util.InstrumentUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ServerboundAdjustInstrumentPacket extends ServerboundUpdateGuiItemPacket {

    private final ResourceLocation instrument;

    public ServerboundAdjustInstrumentPacket(int slot, ResourceLocation instrument){
        super(slot);
        this.instrument = instrument;
    }

    @Override
    public void encoder(FriendlyByteBuf buffer) {
        super.encoder(buffer);
        buffer.writeResourceLocation(this.instrument);
    }

    public static ServerboundAdjustInstrumentPacket decoder(FriendlyByteBuf buffer) {
        return new ServerboundAdjustInstrumentPacket(buffer.readInt(), buffer.readResourceLocation());
    }

    @Override
    protected void updateGuiItem(ServerPlayer sender, ItemStack itemInSlot) {
        if (itemInSlot.getItem() instanceof AdjustableInstrumentItem) {
            InstrumentUtil.setSoundVariantId(itemInSlot, this.instrument);
        }
    }
}
