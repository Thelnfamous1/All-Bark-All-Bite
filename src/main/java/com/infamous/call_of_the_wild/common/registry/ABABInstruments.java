package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.common.util.MiscUtil;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Instrument;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ABABInstruments {

    public static final DeferredRegister<Instrument> INSTRUMENTS = DeferredRegister.create(Registry.INSTRUMENT_REGISTRY, AllBarkAllBite.MODID);

    private static final int WHISTLE_DURATION = MiscUtil.seconds(1);
    private static final int WHISTLE_SOUND_RANGE = 16;

    private static final String SIT_WHISTLE_NAME = String.format("%s_%s", "sit", ABABItems.WHISTLE_NAME);
    private static final String COME_WHISTLE_NAME = String.format("%s_%s", "come", ABABItems.WHISTLE_NAME);
    private static final String GO_WHISTLE_NAME = String.format("%s_%s", "go", ABABItems.WHISTLE_NAME);

    public static final RegistryObject<Instrument> SIT_WHISTLE = INSTRUMENTS.register(SIT_WHISTLE_NAME, () -> new Instrument(SoundEvents.NOTE_BLOCK_FLUTE, WHISTLE_DURATION, WHISTLE_SOUND_RANGE));
    public static final RegistryObject<Instrument> COME_WHISTLE = INSTRUMENTS.register(COME_WHISTLE_NAME, () -> new Instrument(SoundEvents.NOTE_BLOCK_FLUTE, WHISTLE_DURATION, WHISTLE_SOUND_RANGE));
    public static final RegistryObject<Instrument> GO_WHISTLE = INSTRUMENTS.register(GO_WHISTLE_NAME, () -> new Instrument(SoundEvents.NOTE_BLOCK_FLUTE, WHISTLE_DURATION, WHISTLE_SOUND_RANGE));
}
