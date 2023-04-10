package com.infamous.all_bark_all_bite.common.registry;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ABABGameEvents {

    public static DeferredRegister<GameEvent> GAME_EVENTS = DeferredRegister.create(Registries.GAME_EVENT, AllBarkAllBite.MODID);

    private static final String ENTITY_HOWL_NAME = "entity_howl";
    public static RegistryObject<GameEvent> ENTITY_HOWL = GAME_EVENTS.register(ENTITY_HOWL_NAME, () -> new GameEvent(ENTITY_HOWL_NAME, 64));

}
