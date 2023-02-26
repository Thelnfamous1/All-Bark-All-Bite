package com.infamous.all_bark_all_bite.common;

import com.google.common.collect.Maps;
import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.entity.DogSpawner;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.common.entity.dog.DogHooks;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfAi;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfHooks;
import com.infamous.all_bark_all_bite.common.goal.LookAtTargetSinkGoal;
import com.infamous.all_bark_all_bite.common.goal.MoveToTargetSinkGoal;
import com.infamous.all_bark_all_bite.common.item.PetWhistleItem;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABItems;
import com.infamous.all_bark_all_bite.common.util.*;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = AllBarkAllBite.MODID)
public class ForgeEventHandler {
    private static final Map<ResourceKey<Level>, List<CustomSpawner>> CUSTOM_SPAWNERS = Maps.newLinkedHashMap();
    private static final String FOX_IS_DEFENDING = "m_28567_";

    static {
        ArrayList<CustomSpawner> overworldSpawners = Lists.newArrayList();
        overworldSpawners.add(new DogSpawner());
        CUSTOM_SPAWNERS.put(Level.OVERWORLD, overworldSpawners);
    }

    @SubscribeEvent
    static void onWorldTick(TickEvent.LevelTickEvent event){
        if(event.level instanceof ServerLevel serverLevel && event.phase == TickEvent.Phase.END){
            MinecraftServer server = serverLevel.getServer();
            ResourceKey<Level> dimension = serverLevel.dimension();
            List<CustomSpawner> customSpawners = CUSTOM_SPAWNERS.get(dimension);
            if(customSpawners != null) customSpawners.forEach(cs -> cs.tick(serverLevel, server.isSpawningMonsters(), server.isSpawningAnimals()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onEntityJoinLevel(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide) return;
        Entity entity = event.getEntity();

        // add dog interaction behaviors to specific mobs
        addMobDogInteractionGoals(entity);

        // make the Wolf brain
        if(entity.getType() == EntityType.WOLF){
            WolfHooks.onWolfJoinLevel((Wolf)entity, event.loadedFromDisk());
        }

        // add Brain-like pathfinding behaviors for non-Brain-using pets so the "Come" and "Go" whistle commands work for them
        if(entity instanceof PathfinderMob pathfinderMob
                && !pathfinderMob.getBrain().checkMemory(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED)
                && (entity instanceof OwnableEntity || entity instanceof AbstractHorse)){
            pathfinderMob.goalSelector.addGoal(0, new MoveToTargetSinkGoal(pathfinderMob));
            pathfinderMob.goalSelector.addGoal(0, new LookAtTargetSinkGoal(pathfinderMob));
        }
    }

    private static void addMobDogInteractionGoals(Entity mob) {
        EntityType<?> type = mob.getType();
        if(mob instanceof Fox fox && type.is(ABABTags.DOG_ALWAYS_HOSTILES)){
            //noinspection ConstantConditions
            fox.goalSelector.addGoal(4, new AvoidEntityGoal<>(fox, Dog.class, 8.0F, 1.6D, 1.4D,
                    (le) -> !((Dog)le).isTame() && !(boolean) ReflectionUtil.callMethod(FOX_IS_DEFENDING, fox)));
        }
        if(mob instanceof Rabbit rabbit && type.is(ABABTags.DOG_HUNT_TARGETS)){
            rabbit.goalSelector.addGoal(4, new AvoidEntityGoal<>(rabbit, Dog.class, 10.0F, 2.2D, 2.2D){
                @Override
                public boolean canUse() {
                    return rabbit.getRabbitType() != 99 && super.canUse();
                }
            });
        }
        if(mob instanceof AbstractSkeleton skeleton && type.is(ABABTags.DOG_ALWAYS_HOSTILES)){
            skeleton.goalSelector.addGoal(3, new AvoidEntityGoal<>(skeleton, Dog.class, 6.0F, 1.0D, 1.2D));
        }
        if(mob instanceof Llama llama && type.is(ABABTags.DOG_DISLIKED)){
            llama.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(llama, Dog.class, 16, false, true,
                    (le) -> !((Dog)le).isTame()){
                @Override
                protected double getFollowDistance() {
                    return super.getFollowDistance() * 0.25D;
                }
            });
        }
    }

    @SubscribeEvent
    static void onEntitySize(EntityEvent.Size event){
        Entity entity = event.getEntity();
        if(entity.getType() == EntityType.WOLF){
            EntityDimensions resize = WolfHooks.onWolfSize(entity, event.getNewSize());
            event.setNewSize(resize, true);
        } else if(entity.getType() == ABABEntityTypes.DOG.get()){
            EntityDimensions newSize = DogHooks.onDogSize(entity, event.getNewSize());
            event.setNewSize(newSize, true);
        }
    }

    @SubscribeEvent
    static void onLivingUpdate(LivingEvent.LivingTickEvent event){
        LivingEntity entity = event.getEntity();
        Level level = entity.level;
        if(!event.isCanceled()
                && entity.getType() == EntityType.WOLF
                && !level.isClientSide){
            WolfHooks.onWolfUpdate((Wolf)entity, (ServerLevel) level);
        }
    }

    @SubscribeEvent
    static void onLivingFall(LivingFallEvent event){
        if(event.getEntity().getType() == EntityType.WOLF){
            event.setDistance(event.getDistance() - (SharedWolfAi.FALL_REDUCTION - AiUtil.DEFAULT_FALL_REDUCTION));
        }
    }

    @SubscribeEvent
    static void onSleepPosCheck(SleepingLocationCheckEvent event){
        EntityType<?> type = event.getEntity().getType();
        if(type == EntityType.WOLF || type == ABABEntityTypes.DOG.get()){
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
        if (event.isCanceled()) {
            return;
        }

        Player player = event.getEntity();
        if(!player.isSpectator() && CompatUtil.isDILoaded()){
            Level level = player.getLevel();
            BlockPos blockPos = event.getPos();
            boolean canSneakBypass = !player.isHolding(is -> is.doesSneakBypassUse(level, blockPos, player));
            boolean sneakBypass = player.isSecondaryUseActive() && canSneakBypass;
            Event.Result useBlock = event.getUseBlock();
            if (useBlock == Event.Result.ALLOW || (useBlock != Event.Result.DENY && !sneakBypass)) {
                BlockState blockState = level.getBlockState(blockPos);
                DICompat.handleDIDrum(player, level, blockPos, blockState);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event){
        if(event.isCanceled()) return;
        Player player = event.getEntity();
        Entity target = event.getTarget();

        if(event.getItemStack().is(ABABItems.WHISTLE.get())){
            if(PetWhistleItem.interactWithPet(event.getItemStack(), player, target, event.getHand())){
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }

        if(!event.getItemStack().is(ABABTags.HAS_WOLF_INTERACTION) && !player.isSecondaryUseActive()){
            if(target.getType() == EntityType.WOLF){
                event.setCanceled(true);
                event.setCancellationResult(AiUtil.interactOn(player, (Wolf)target, event.getHand(), WolfAi::mobInteract));
            }
        }
    }

    @SubscribeEvent
    static void onBabySpawn(BabyEntitySpawnEvent event){
        AgeableMob child = event.getChild();
        if(child != null && child.getType() == EntityType.WOLF){
            WolfHooks.onWolfPupSpawn((Wolf)child, event.getCausedByPlayer());
        }
    }

    @SubscribeEvent
    static void onItemUseStart(LivingEntityUseItemEvent.Start event){
        if(event.getEntity().getLevel() instanceof ServerLevel serverLevel && event.getItem().is(ABABItems.WHISTLE.get())){
            PetWhistleItem.onItemUseStart(event.getEntity(), event.getItem(), serverLevel);
        }
    }

    @SubscribeEvent
    static void onLivingJump(LivingEvent.LivingJumpEvent event){
        LivingEntity entity = event.getEntity();
        if(entity.getType() == EntityType.WOLF){
            WolfHooks.onWolfJump(entity);
        }
    }

    @SubscribeEvent
    static void onServerStarting(ServerAboutToStartEvent event){
        List<? extends Integer> houndmasterRaidWaveCounts = ABABConfig.houndmasterRaidWaveCounts.get();
        int[] waveCounts = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        for(int i = 1; i < waveCounts.length; i++){
            try{
                waveCounts[i] = houndmasterRaidWaveCounts.get(i - 1);
            } catch (Exception ignored){
                waveCounts[i] = 0;
            }
        }
        AllBarkAllBite.LOGGER.info("Adding RaiderType for entity type {} with wave counts {}", ABABEntityTypes.HOUNDMASTER.get(), Arrays.toString(waveCounts));
        Raid.RaiderType.create(ABABEntityTypes.HOUNDMASTER_NAME, ABABEntityTypes.HOUNDMASTER.get(), waveCounts);
    }

}
