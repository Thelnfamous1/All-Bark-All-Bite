package com.infamous.all_bark_all_bite.common;

import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.common.entity.houndmaster.Houndmaster;
import com.infamous.all_bark_all_bite.common.entity.illager_hound.IllagerHound;
import com.infamous.all_bark_all_bite.common.logic.ABABRaiderTypes;
import com.infamous.all_bark_all_bite.common.network.ABABNetwork;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = AllBarkAllBite.MODID)
public class ModEventHandler {

    @SubscribeEvent
    static void onRegisterSpawnPlacements(SpawnPlacementRegisterEvent event){
        event.register(ABABEntityTypes.DOG.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ABABEntityTypes.ILLAGER_HOUND.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);

        event.register(ABABEntityTypes.HOUNDMASTER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PatrollingMonster::checkPatrollingMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    static void onEntityAttributeCreation(EntityAttributeCreationEvent event){
        event.put(ABABEntityTypes.DOG.get(), Dog.createAttributes().build());
        event.put(ABABEntityTypes.ILLAGER_HOUND.get(), IllagerHound.createAttributes().build());
        event.put(ABABEntityTypes.HOUNDMASTER.get(), Houndmaster.createAttributes().build());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onEntityAttributeModification(EntityAttributeModificationEvent event){
        event.add(EntityType.WOLF, Attributes.ATTACK_DAMAGE, 5.0D);
        event.add(EntityType.WOLF, Attributes.MAX_HEALTH, 25.0D);
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event){
        event.enqueueWork( () -> {
            ABABNetwork.register();
            ABABRaiderTypes.initHoundmasterRaiderType();
        });
    }

}
