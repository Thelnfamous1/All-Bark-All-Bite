package com.infamous.call_of_the_wild.common.registry;

import com.infamous.call_of_the_wild.AllBarkAllBite;
import net.minecraft.core.Registry;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ABABGameEvents {

    public static DeferredRegister<GameEvent> GAME_EVENTS = DeferredRegister.create(Registry.GAME_EVENT_REGISTRY, AllBarkAllBite.MODID);

    private static final String ENTITY_HOWL_NAME = "entity_howl";
    public static RegistryObject<GameEvent> ENTITY_HOWL = GAME_EVENTS.register(ENTITY_HOWL_NAME, () -> new GameEvent(ENTITY_HOWL_NAME, 64));

}
