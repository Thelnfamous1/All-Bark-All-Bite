package com.infamous.all_bark_all_bite.common.util;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class InstrumentUtil {
    public static final String INSTRUMENT_TAG = "instrument";

    public static Optional<? extends Holder<Instrument>> getInstrumentHolder(ResourceLocation location) {
        return BuiltInRegistries.INSTRUMENT.getHolder(ResourceKey.create(Registries.INSTRUMENT, location));
    }

    public static void setSoundVariantId(ItemStack stack, Holder<Instrument> instrument) {
        setSoundVariantId(stack, getInstrumentLocation(instrument));
    }

    public static ResourceLocation getInstrumentLocation(Holder<Instrument> instrument) {
        return instrument.unwrapKey().orElseThrow(() -> new IllegalStateException("Invalid instrument")).location();
    }

    public static void setSoundVariantId(ItemStack itemStack, ResourceLocation location) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString(INSTRUMENT_TAG, location.toString());
    }

    public static MutableComponent getInstrumentTooltip(Holder<Instrument> instrument) {
        return Component.translatable(makeInstrumentDescriptionId(getInstrumentLocation(instrument)));
    }

    public static MutableComponent getInstrumentDescriptionTooltip(Holder<Instrument> instrument){
        return Component.translatable(makeInstrumentDescription(getInstrumentLocation(instrument)));
    }

    @SuppressWarnings("SameParameterValue")
    public static String makeInstrumentDescriptionId(ResourceLocation location) {
        return Util.makeDescriptionId(INSTRUMENT_TAG, location);
    }

    public static String makeInstrumentDescription(ResourceLocation location) {
        return makeInstrumentDescriptionId(location) + ".description";
    }
}
