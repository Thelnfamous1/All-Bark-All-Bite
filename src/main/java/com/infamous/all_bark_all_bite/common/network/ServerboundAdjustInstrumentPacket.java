package com.infamous.all_bark_all_bite.common.network;

import com.infamous.all_bark_all_bite.common.item.AdjustableInstrumentItem;
import com.infamous.all_bark_all_bite.common.util.InstrumentUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundAdjustInstrumentPacket {

    private final int slot;
    private final ResourceLocation instrument;

    public ServerboundAdjustInstrumentPacket(int slot, ResourceLocation instrument){
        this.slot = slot;
        this.instrument = instrument;
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(this.slot);
        buffer.writeResourceLocation(this.instrument);
    }

    public static ServerboundAdjustInstrumentPacket decoder(FriendlyByteBuf buffer) {
        return new ServerboundAdjustInstrumentPacket(buffer.readInt(), buffer.readResourceLocation());
    }

    public void messageConsumer(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            int slot = this.slot;
            if (Inventory.isHotbarSlot(slot) || slot == Inventory.SLOT_OFFHAND) {
                this.updateInstrument(sender, slot);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void updateInstrument(ServerPlayer sender, int slot) {
        ItemStack itemInSlot = sender.getInventory().getItem(slot);
        if (itemInSlot.getItem() instanceof AdjustableInstrumentItem) {
            InstrumentUtil.setSoundVariantId(itemInSlot, this.instrument);
        }
    }
}
