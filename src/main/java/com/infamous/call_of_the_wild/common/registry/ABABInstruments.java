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
    public static final RegistryObject<Instrument> ATTACK_WHISTLE = registerWhistle("attack");

    public static final RegistryObject<Instrument> COME_WHISTLE = registerWhistle("come");

    public static final RegistryObject<Instrument> FREE_WHISTLE = registerWhistle("free");

    public static final RegistryObject<Instrument> GO_WHISTLE = registerWhistle("go");

    public static final RegistryObject<Instrument> HEEL_WHISTLE = registerWhistle("heel");

    public static final RegistryObject<Instrument> SIT_WHISTLE = registerWhistle("sit");

    private static RegistryObject<Instrument> registerWhistle(String come) {
        return INSTRUMENTS.register(makeWhistleName(come), () -> new Instrument(SoundEvents.NOTE_BLOCK_FLUTE, WHISTLE_DURATION, WHISTLE_SOUND_RANGE));
    }
    private static String makeWhistleName(String come) {
        return String.format("%s_%s", come, ABABItems.WHISTLE_NAME);
    }
}
