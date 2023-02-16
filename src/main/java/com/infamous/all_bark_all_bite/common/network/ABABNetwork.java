package com.infamous.all_bark_all_bite.common.network;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public class ABABNetwork {
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AllBarkAllBite.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register(){
        INSTANCE.registerMessage(COUNTER.getAndIncrement(), ServerboundAdjustInstrumentPacket.class, ServerboundAdjustInstrumentPacket::encoder, ServerboundAdjustInstrumentPacket::decoder, ServerboundAdjustInstrumentPacket::messageConsumer);
    }
}
