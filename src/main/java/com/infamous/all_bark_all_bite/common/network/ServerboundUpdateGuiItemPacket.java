package com.infamous.all_bark_all_bite.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class ServerboundUpdateGuiItemPacket {
    protected final int slot;

    public ServerboundUpdateGuiItemPacket(int slot) {
        this.slot = slot;
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(this.slot);
    }

    public void messageConsumer(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            int slot = this.slot;
            if (Inventory.isHotbarSlot(slot) || slot == Inventory.SLOT_OFFHAND) {
                ItemStack itemInSlot = sender.getInventory().getItem(slot);
                this.updateGuiItem(sender, itemInSlot);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    protected abstract void updateGuiItem(ServerPlayer sender, ItemStack itemInSlot);
}
