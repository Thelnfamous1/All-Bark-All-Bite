package com.infamous.all_bark_all_bite.common;

import com.google.common.collect.Maps;
import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ai.TrustAi;
import com.infamous.all_bark_all_bite.common.entity.DogSpawner;
import com.infamous.all_bark_all_bite.common.entity.EntityAnimationController;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfAi;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfBrain;
import com.infamous.all_bark_all_bite.common.event.BrainEvent;
import com.infamous.all_bark_all_bite.common.goal.LookAtTargetSinkGoal;
import com.infamous.all_bark_all_bite.common.goal.MoveToTargetSinkGoal;
import com.infamous.all_bark_all_bite.common.item.PetWhistleItem;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABItems;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import com.infamous.all_bark_all_bite.common.util.CompatUtil;
import com.infamous.all_bark_all_bite.common.util.DebugUtil;
import com.infamous.all_bark_all_bite.common.util.ReflectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
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
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
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

    @SubscribeEvent
    static void onMakeBrain(BrainEvent.MakeBrain event){
        if(event.getEntity().getType() == EntityType.WOLF){
            Brain<Wolf> replacement = WolfBrain.makeBrain(event.makeBrain(WolfAi.MEMORY_TYPES, WolfAi.SENSOR_TYPES));
            event.setNewBrain(replacement);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onEntityJoinLevel(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide) return;
        Entity entity = event.getEntity();
        if(entity instanceof PathfinderMob pathfinderMob
                && entity.getType() != ABABEntityTypes.DOG.get()
                && (entity instanceof OwnableEntity || entity instanceof AbstractHorse)){
            pathfinderMob.goalSelector.addGoal(0, new MoveToTargetSinkGoal(pathfinderMob));
            pathfinderMob.goalSelector.addGoal(0, new LookAtTargetSinkGoal(pathfinderMob));
        }
        addMobDogInteractionGoals(entity);

        if(entity instanceof Wolf wolf && entity.getType() == EntityType.WOLF){
            wolf.goalSelector.removeAllGoals();
            wolf.targetSelector.removeAllGoals();
            if(!event.loadedFromDisk()) {
                WolfAi.initMemories(wolf, wolf.getRandom());
            }
        }
    }

    private static void addMobDogInteractionGoals(Entity entity) {
        if(entity instanceof Fox fox && fox.getType().is(ABABTags.DOG_ALWAYS_HOSTILES)){
            //noinspection ConstantConditions
            fox.goalSelector.addGoal(4, new AvoidEntityGoal<>(fox, Dog.class, 8.0F, 1.6D, 1.4D,
                    (le) -> !((Dog)le).isTame() && !(boolean) ReflectionUtil.callMethod(FOX_IS_DEFENDING, fox)));
        }
        if(entity instanceof Rabbit rabbit && rabbit.getType().is(ABABTags.DOG_HUNT_TARGETS)){
            rabbit.goalSelector.addGoal(4, new AvoidEntityGoal<>(rabbit, Dog.class, 10.0F, 2.2D, 2.2D){
                @Override
                public boolean canUse() {
                    return rabbit.getRabbitType() != 99 && super.canUse();
                }
            });
        }
        if(entity instanceof AbstractSkeleton skeleton && skeleton.getType().is(ABABTags.DOG_ALWAYS_HOSTILES)){
            skeleton.goalSelector.addGoal(3, new AvoidEntityGoal<>(skeleton, Dog.class, 6.0F, 1.0D, 1.2D));
        }
        if(entity instanceof Llama llama && llama.getType().is(ABABTags.DOG_DISLIKED)){
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
            EntityDimensions newSize = event.getNewSize();
            newSize = resetIfSleeping(entity, newSize);
            newSize = unfixIfNeeded(newSize);
            EntityDimensions resize = newSize.scale(WolfAi.WOLF_SIZE_SCALE);
            resize = resizeForLongJumpIfNeeded(entity, resize);
            event.setNewSize(resize, true);
        } else if(entity.getType() == ABABEntityTypes.DOG.get()){
            EntityDimensions newSize = event.getNewSize();
            newSize = resetIfSleeping(entity, newSize);
            newSize = unfixIfNeeded(newSize);
            newSize = resizeForLongJumpIfNeeded(entity, newSize);
            event.setNewSize(newSize, true);
        }
    }

    private static EntityDimensions resetIfSleeping(Entity entity, EntityDimensions newSize) {
        if(entity.hasPose(Pose.SLEEPING)){
            newSize = entity.getDimensions(Pose.STANDING);
        }
        return newSize;
    }

    private static EntityDimensions unfixIfNeeded(EntityDimensions newSize) {
        if(newSize.fixed){
            newSize = new EntityDimensions(newSize.width, newSize.height, false);
        }
        return newSize;
    }

    private static EntityDimensions resizeForLongJumpIfNeeded(Entity entity, EntityDimensions resize) {
        if(entity.hasPose(Pose.LONG_JUMPING)){
            resize = resize.scale(SharedWolfAi.LONG_JUMPING_SCALE);
        }
        return resize;
    }

    @SubscribeEvent
    static void onLivingUpdate(LivingEvent.LivingTickEvent event){
        LivingEntity livingEntity = event.getEntity();
        if(!event.isCanceled()
                && livingEntity instanceof Wolf wolf
                && livingEntity.getType() == EntityType.WOLF
                && wolf.level instanceof ServerLevel level){
            WolfAi.updateAi(level, wolf);
            DebugUtil.sendEntityBrain(wolf, level,
                    ABABMemoryModuleTypes.TRUST.get(),
                    ABABMemoryModuleTypes.MAX_TRUST.get(),
                    ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get(),
                    ABABMemoryModuleTypes.IS_ORDERED_TO_HEEL.get(),
                    ABABMemoryModuleTypes.IS_ORDERED_TO_SIT.get());
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
                CompatUtil.handleDIDrum(player, level, blockPos, blockState);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event){
        if(event.isCanceled()) return;

        if(event.getItemStack().is(ABABItems.WHISTLE.get())){
            if(PetWhistleItem.interactWithPet(event.getItemStack(), event.getEntity(), event.getTarget(), event.getHand())){
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }
        }

        if(!event.getItemStack().is(ABABTags.HAS_WOLF_INTERACTION) && !event.getEntity().isSecondaryUseActive()){
            Entity target = event.getTarget();
            if(target instanceof Wolf wolf && target.getType() == EntityType.WOLF){
                event.setCanceled(true);
                event.setCancellationResult(AiUtil.interactOn(event.getEntity(), wolf, event.getHand(), WolfAi::mobInteract));
            }
        }
    }

    @SubscribeEvent
    static void onBabySpawn(BabyEntitySpawnEvent event){
        AgeableMob child = event.getChild();
        if(child instanceof Wolf pup && child.getType() == EntityType.WOLF){
            Player player = event.getCausedByPlayer();
            if(player != null){
                TrustAi.setTrust(pup, 0);
                TrustAi.setRandomMaxTrust(pup, WolfAi.MAX_TRUST);
                TrustAi.setLikedPlayer(pup, player);
            }
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
            entity.level.broadcastEntityEvent(entity, EntityAnimationController.JUMPING_EVENT_ID);
        }
    }
}
