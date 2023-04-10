package com.infamous.all_bark_all_bite.common.registry;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Instrument;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ABABInstruments {

    public static final DeferredRegister<Instrument> INSTRUMENTS = DeferredRegister.create(Registries.INSTRUMENT, AllBarkAllBite.MODID);

    public static final int WHISTLE_DURATION = MiscUtil.seconds(1);
    private static final int WHISTLE_SOUND_RANGE = 16;
    public static final RegistryObject<Instrument> ATTACK_WHISTLE = registerWhistle("attack", ABABSoundEvents.ATTACK_WHISTLE);

    public static final RegistryObject<Instrument> COME_WHISTLE = registerWhistle("come", ABABSoundEvents.COME_WHISTLE);

    public static final RegistryObject<Instrument> FOLLOW_WHISTLE = registerWhistle("follow", ABABSoundEvents.FOLLOW_WHISTLE);

    public static final RegistryObject<Instrument> FREE_WHISTLE = registerWhistle("free", ABABSoundEvents.FREE_WHISTLE);

    public static final RegistryObject<Instrument> GO_WHISTLE = registerWhistle("go", ABABSoundEvents.GO_WHISTLE);

    public static final RegistryObject<Instrument> HEEL_WHISTLE = registerWhistle("heel", ABABSoundEvents.HEEL_WHISTLE);

    public static final RegistryObject<Instrument> SIT_WHISTLE = registerWhistle("sit", ABABSoundEvents.SIT_WHISTLE);

    private static RegistryObject<Instrument> registerWhistle(String come, RegistryObject<SoundEvent> whistle) {
        return INSTRUMENTS.register(makeWhistleName(come), () -> new Instrument(whistle.getHolder().get(), WHISTLE_DURATION, WHISTLE_SOUND_RANGE));
    }
    private static String makeWhistleName(String come) {
        return String.format("%s_%s", come, ABABItems.WHISTLE_NAME);
    }
}
