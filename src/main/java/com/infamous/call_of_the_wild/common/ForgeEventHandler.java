package com.infamous.call_of_the_wild.common;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.infamous.call_of_the_wild.AllBarkAllBite;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.ai.BrainUtil;
import com.infamous.call_of_the_wild.common.entity.DogSpawner;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.infamous.call_of_the_wild.common.entity.dog.DogAi;
import com.infamous.call_of_the_wild.common.entity.wolf.WolfAi;
import com.infamous.call_of_the_wild.common.event.BrainEvent;
import com.infamous.call_of_the_wild.common.registry.ABABEntityTypes;
import com.infamous.call_of_the_wild.common.registry.ABABInstruments;
import com.infamous.call_of_the_wild.common.registry.ABABItems;
import com.infamous.call_of_the_wild.common.util.DebugUtil;
import com.infamous.call_of_the_wild.common.util.MultiEntityManager;
import com.infamous.call_of_the_wild.common.util.PetManagement;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
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
            Brain<Wolf> replacement = WolfAi.makeBrain(event.makeBrain(WolfAi.MEMORY_TYPES, WolfAi.SENSOR_TYPES));
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
        if(event.getEntity().getType() == EntityType.WOLF){
            EntityDimensions newSize = event.getNewSize();
            EntityDimensions resize = newSize.scale(WolfAi.WOLF_SIZE_SCALE);
            if(event.getEntity().hasPose(Pose.LONG_JUMPING)){
                resize = resize.scale(WolfAi.WOLF_SIZE_LONG_JUMPING_SCALE);
            }
            event.setNewSize(resize, true);
        }
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
    static void onSleepPosCheck(SleepingLocationCheckEvent event){
        if(event.getEntity().getType() == EntityType.WOLF){
            event.setResult(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    static void onLivingFall(LivingFallEvent event){
        LivingEntity livingEntity = event.getEntity();
        if(!event.isCanceled()
                && livingEntity instanceof Wolf
                && livingEntity.getType() == EntityType.WOLF){
            event.setDistance(event.getDistance() - 5);
        }
    }

    @SubscribeEvent
    static void onLivingChangeTarget(LivingChangeTargetEvent event){
        if(event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.BEHAVIOR_TARGET
                && event.getEntity() instanceof NeutralMob neutralMob
                && event.getEntity().getType() == EntityType.WOLF){
            neutralMob.setTarget(event.getNewTarget());
        }
    }

    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event){
        Entity target = event.getTarget();
        if(target instanceof Wolf wolf && target.getType() == EntityType.WOLF){
            event.setCanceled(true);
            event.setCancellationResult(AiUtil.interactOn(event.getEntity(), wolf, event.getHand(), WolfAi::mobInteract));
        }
    }

    @SubscribeEvent
    static void onBabySpawn(BabyEntitySpawnEvent event){
        AgeableMob child = event.getChild();
        if(child instanceof Wolf wolf && child.getType() == EntityType.WOLF){
            Player player = event.getCausedByPlayer();
            if(player != null){
                wolf.tame(player);
            }
        }
    }

    @SubscribeEvent
    static void onVanillaGameEvent(VanillaGameEvent event){
        if(event.getVanillaEvent() == GameEvent.INSTRUMENT_PLAY && event.getCause() instanceof Player player){
            ItemStack useItem = player.getUseItem();
            if(useItem.is(ABABItems.WHISTLE.get())){
                Optional<Holder<Instrument>> instrumentHolder = ABABItems.WHISTLE.get().getInstrument(useItem);
                if(instrumentHolder.isPresent()){
                    Instrument instrument = instrumentHolder.get().value();
                    MultiEntityManager petManager = PetManagement.getPetManager(event.getLevel().dimension(), player.getUUID());
                    if(instrument == ABABInstruments.SIT_WHISTLE.get()){
                        petManager.stream().forEach(entity -> {
                            if(entity instanceof Dog dog){
                                DogAi.commandSit(dog);
                            }
                        });
                    }
                    if(instrument == ABABInstruments.COME_WHISTLE.get()){
                        petManager.stream().forEach(entity -> {
                            if(entity instanceof Dog dog){
                                DogAi.commandCome(dog);
                            }
                        });
                    }
                    if(instrument == ABABInstruments.GO_WHISTLE.get()){
                        petManager.stream().forEach(entity -> {
                            if(entity instanceof Dog dog){
                                DogAi.commandGo(dog);
                            }
                        });
                    }
                }
            }
        }
    }
}
