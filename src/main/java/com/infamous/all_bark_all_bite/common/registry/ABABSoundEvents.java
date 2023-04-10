package com.infamous.all_bark_all_bite.common.registry;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ABABSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AllBarkAllBite.MODID);

    public static final RegistryObject<SoundEvent> ATTACK_WHISTLE = registerWhistle("attack");

    public static final RegistryObject<SoundEvent> COME_WHISTLE = registerWhistle("come");

    public static final RegistryObject<SoundEvent> FOLLOW_WHISTLE = registerWhistle("follow");

    public static final RegistryObject<SoundEvent> FREE_WHISTLE = registerWhistle("free");

    public static final RegistryObject<SoundEvent> GO_WHISTLE = registerWhistle("go");

    public static final RegistryObject<SoundEvent> HEEL_WHISTLE = registerWhistle("heel");

    public static final RegistryObject<SoundEvent> SIT_WHISTLE = registerWhistle("sit");


    private static RegistryObject<SoundEvent> registerWhistle(String name) {
        return registerVariableRangeSoundEvent(String.format("%s_whistle", name));
    }

    private static RegistryObject<SoundEvent> registerVariableRangeSoundEvent(String path) {
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AllBarkAllBite.MODID, path)));
    }
}
