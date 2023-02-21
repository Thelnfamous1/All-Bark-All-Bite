package com.infamous.all_bark_all_bite.common;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.infamous.all_bark_all_bite.AllBarkAllBite;
import com.infamous.all_bark_all_bite.common.ai.CommandAi;
import com.infamous.all_bark_all_bite.common.ai.TrustAi;
import com.infamous.all_bark_all_bite.common.entity.DogSpawner;
import com.infamous.all_bark_all_bite.common.entity.EntityAnimationController;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.entity.dog.Dog;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfAi;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfBrain;
import com.infamous.all_bark_all_bite.common.event.BrainEvent;
import com.infamous.all_bark_all_bite.common.logic.PetManagement;
import com.infamous.all_bark_all_bite.common.logic.entity_manager.MultiEntityManager;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABInstruments;
import com.infamous.all_bark_all_bite.common.registry.ABABItems;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import com.infamous.all_bark_all_bite.common.util.BrainUtil;
import com.infamous.all_bark_all_bite.common.util.DebugUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
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

import java.util.*;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = AllBarkAllBite.MODID)
public class ForgeEventHandler {
    private static final Map<ResourceKey<Level>, List<CustomSpawner>> CUSTOM_SPAWNERS = Maps.newLinkedHashMap();
    //private static final String FOX_IS_DEFENDING = "m_28567_";

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

    @SubscribeEvent
    static void onVillagerRefresh(BrainEvent.VillagerRefresh event){
        addVillagerDogInteractionBehaviors(event.getNewBrain());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onEntityJoinLevel(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide) return;
        Entity entity = event.getEntity();
        addMobDogInteractionGoals(entity);

        if(entity instanceof Villager villager){
            addVillagerDogInteractionBehaviors(villager.getBrain());
        }

        if(entity instanceof Wolf wolf && entity.getType() == EntityType.WOLF){
            wolf.goalSelector.removeAllGoals();
            wolf.targetSelector.removeAllGoals();
            if(!event.loadedFromDisk()) {
                WolfAi.initMemories(wolf, wolf.getRandom());
            }
        }
    }

    private static void addMobDogInteractionGoals(Entity entity) {
        /*
        if(entity instanceof Fox fox){
            fox.goalSelector.addGoal(4, new AvoidEntityGoal<>(fox, Dog.class, 8.0F, 1.6D, 1.4D,
                    (le) -> !((Dog)le).isTame() && !(boolean) ReflectionUtil.callMethod(FOX_IS_DEFENDING, fox)));
        }
         */
        if(entity instanceof Rabbit rabbit){
            rabbit.goalSelector.addGoal(4, new AvoidEntityGoal<>(rabbit, Dog.class, 10.0F, 2.2D, 2.2D){
                @Override
                public boolean canUse() {
                    return rabbit.getRabbitType() != 99 && super.canUse();
                }
            });
        }
        if(entity instanceof AbstractSkeleton skeleton){
            skeleton.goalSelector.addGoal(3, new AvoidEntityGoal<>(skeleton, Dog.class, 6.0F, 1.0D, 1.2D));
        }
        /*
        if(entity instanceof Llama llama){
            llama.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(llama, Dog.class, 16, false, true,
                    (le) -> !((Dog)le).isTame()){
                @Override
                protected double getFollowDistance() {
                    return super.getFollowDistance() * 0.25D;
                }
            });
        }
         */
    }

    /**
     * See {@link net.minecraft.world.entity.ai.behavior.VillagerGoalPackages}
     */
    private static void addVillagerDogInteractionBehaviors(Brain<Villager> brain) {
        Map<Integer, Map<Activity, Set<Behavior<? super Villager>>>> availableBehaviorsByPriority = BrainUtil.getAvailableBehaviorsByPriority(brain);
        for(Integer priority : availableBehaviorsByPriority.keySet()){
            if(priority != 2 && priority != 5) continue; // Villager RunOne behaviors that make them look at or interact with cats are only of priority 2 or 5

            Map<Activity, Set<Behavior<? super Villager>>> availableBehaviors = availableBehaviorsByPriority.get(priority);

            boolean addedPlayLook = false; // Look behavior is added before interact behavior for PLAY
            boolean addedPlayInteract = false;
            for(Behavior<?> behavior : availableBehaviors.getOrDefault(Activity.PLAY, ImmutableSet.of())){
                if(addedPlayLook && addedPlayInteract) break;
                if(behavior instanceof RunOne<?> runOne){
                    if(!addedPlayLook){
                        BrainUtil.getGateBehaviors(runOne).add(new SetEntityLookTarget(ABABEntityTypes.DOG.get(), 8.0F), 8);
                        addedPlayLook = true;
                        continue;
                    }
                    BrainUtil.getGateBehaviors(runOne).add(InteractWith.of(ABABEntityTypes.DOG.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1);
                    addedPlayInteract = true;
                }
            }

            boolean addedIdleInteract = false; // Interact behavior is added before look behavior for IDLE
            boolean addedIdleLook = false;
            for(Behavior<?> behavior : availableBehaviors.getOrDefault(Activity.IDLE, ImmutableSet.of())){
                if(addedIdleInteract && addedIdleLook) break;
                if(behavior instanceof RunOne<?> runOne){
                    if(!addedIdleInteract){
                        BrainUtil.getGateBehaviors(runOne).add(InteractWith.of(ABABEntityTypes.DOG.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1);
                        addedIdleInteract = true;
                        continue;
                    }
                    BrainUtil.getGateBehaviors(runOne).add(new SetEntityLookTarget(ABABEntityTypes.DOG.get(), 8.0F), 8);
                    addedIdleLook = true;
                }
            }
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
            DebugUtil.sendEntityBrain(wolf, level);
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
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event){
        if(!event.isCanceled() && !event.getItemStack().is(ABABTags.HAS_WOLF_INTERACTION) && !event.getEntity().isSecondaryUseActive()){
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
            LivingEntity user = event.getEntity();
            ItemStack useItem = event.getItem();
            Optional<Holder<Instrument>> instrumentHolder = ABABItems.WHISTLE.get().getInstrument(useItem);
            if(instrumentHolder.isPresent()){
                Instrument instrument = instrumentHolder.get().value();
                MultiEntityManager petManager = PetManagement.getPetManager(user.getLevel().dimension(), user.getUUID());
                if(instrument == ABABInstruments.ATTACK_WHISTLE.get()){
                    AiUtil.getTargetedEntity(user, 16)
                            .filter(LivingEntity.class::isInstance)
                            .map(LivingEntity.class::cast)
                            .ifPresent(target -> commandPet(petManager, dog -> CommandAi.commandAttack(dog, target, user)));
                }
                if(instrument == ABABInstruments.COME_WHISTLE.get()){
                    commandPet(petManager, dog -> CommandAi.commandCome(dog, user, serverLevel));
                }
                if(instrument == ABABInstruments.FOLLOW_WHISTLE.get()){
                    commandPet(petManager, CommandAi::commandFollow);
                }
                if(instrument == ABABInstruments.FREE_WHISTLE.get()){
                    commandPet(petManager, CommandAi::commandFree);
                }
                if(instrument == ABABInstruments.GO_WHISTLE.get()){
                    HitResult hitResult = AiUtil.getHitResult(user, 16);
                    if(hitResult.getType() != HitResult.Type.MISS){
                        commandPet(petManager, dog -> CommandAi.commandGo(dog, hitResult));
                    }
                }
                if(instrument == ABABInstruments.HEEL_WHISTLE.get()){
                    commandPet(petManager, CommandAi::commandHeel);
                }
                if(instrument == ABABInstruments.SIT_WHISTLE.get()){
                    commandPet(petManager, CommandAi::commandSit);
                }
            }
        }
    }

    private static void commandPet(MultiEntityManager petManager, Consumer<TamableAnimal> command) {
        petManager.stream().forEach(pet -> {
            if(pet instanceof TamableAnimal dog){
                command.accept(dog);
            }
        });
    }

    @SubscribeEvent
    static void onLivingJump(LivingEvent.LivingJumpEvent event){
        LivingEntity entity = event.getEntity();
        if(entity.getType() == EntityType.WOLF){
            entity.level.broadcastEntityEvent(entity, EntityAnimationController.JUMPING_EVENT_ID);
        }
    }
}
