package com.infamous.all_bark_all_bite.common.logic;

import com.google.common.collect.Maps;
import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.logic.entity_manager.MultiEntityManager;
import com.infamous.all_bark_all_bite.common.util.PetUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class PetManagement {

    private static final Map<ResourceKey<Level>, Map<UUID, MultiEntityManager>> BY_LEVEL_BY_OWNER = Maps.newHashMap();

    private static void tick(ServerLevel level){
        BY_LEVEL_BY_OWNER.values().forEach(byOwner -> byOwner.forEach((ownerUUID, petManager) -> petManager.tick(level, entity -> isValidPet(ownerUUID, entity))));
    }

    private static boolean isValidPet(UUID ownerUUID, Entity entity){
        return PetUtil.getOwnerUUID(entity).filter(uuid -> uuid.equals(ownerUUID)).isPresent();
    }

    public static MultiEntityManager getPetManager(ResourceKey<Level> dimension, UUID ownerUUID){
        return BY_LEVEL_BY_OWNER
                .computeIfAbsent(dimension, k -> Maps.newHashMap())
                .computeIfAbsent(ownerUUID, k -> new MultiEntityManager(Collections.emptyList()));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = AllBarkAllBite.MODID)
    private static class EventHandler{

        @SubscribeEvent
        static void onEntityJoinLevel(EntityJoinLevelEvent event){
            Level level = event.getLevel();
            if(!level.isClientSide){
                Entity entity = event.getEntity();
                PetUtil.getOwnerUUID(entity)
                        .ifPresent(uuid -> PetManagement.getPetManager(level.dimension(), uuid).add(entity));
            }
        }
        @SubscribeEvent(priority = EventPriority.LOWEST)
        static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            Entity target = event.getTarget();
            Level level = event.getLevel();
            if (!level.isClientSide) {
                UUID playerUUID = player.getUUID();
                PetUtil.getOwnerUUID(target)
                        .filter(uuid -> uuid.equals(playerUUID))
                        .ifPresent(uuid -> PetManagement.getPetManager(level.dimension(), uuid).add(target));
            }
        }

        @SubscribeEvent
        static void onServerTick(TickEvent.LevelTickEvent event){
            if(event.level instanceof ServerLevel serverLevel && event.phase == TickEvent.Phase.END){
                PetManagement.tick(serverLevel);
            }
        }
    }
}
