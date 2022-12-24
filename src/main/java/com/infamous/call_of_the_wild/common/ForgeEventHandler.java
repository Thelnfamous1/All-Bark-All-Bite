package com.infamous.call_of_the_wild.common;

import com.google.common.collect.Maps;
import com.infamous.call_of_the_wild.CallOfTheWild;
import com.infamous.call_of_the_wild.common.entity.DogSpawner;
import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.infamous.call_of_the_wild.common.entity.dog.WolfAi;
import com.infamous.call_of_the_wild.common.util.BrainUtil;
import com.infamous.call_of_the_wild.common.util.ReflectionUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = CallOfTheWild.MODID)
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
        ServerLevel serverLevel = (ServerLevel) event.getLevel();

        Entity entity = event.getEntity();
        handleDogInteractionGoals(entity);
        if(entity instanceof Wolf wolf && entity.getType() == EntityType.WOLF){
            wolf.goalSelector.removeAllGoals();
            wolf.targetSelector.removeAllGoals();
            NbtOps nbtOps = NbtOps.INSTANCE;
            Brain<Wolf> replacement = WolfAi.makeBrain(BrainUtil.makeBrain(WolfAi.MEMORY_TYPES, WolfAi.SENSOR_TYPES, BrainUtil.makeDynamic(nbtOps)));
            BrainUtil.replaceBrain(wolf, serverLevel, replacement, event.loadedFromDisk());
        }
    }

    private static void handleDogInteractionGoals(Entity entity) {
        if(entity instanceof Fox fox){
            fox.goalSelector.addGoal(4, new AvoidEntityGoal<>(fox, Dog.class, 8.0F, 1.6D, 1.4D,
                    (le) -> !((Dog)le).isTame() && !(boolean) ReflectionUtil.callMethod(FOX_IS_DEFENDING, fox)));
        }
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
        if(entity instanceof Llama llama){
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
        if(event.getEntity().getType() == EntityType.WOLF){
            EntityDimensions newSize = event.getNewSize();
            event.setNewSize(newSize.scale(1.25F), true);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    static void onLivingUpdate(LivingEvent.LivingTickEvent event){
        LivingEntity livingEntity = event.getEntity();
        if(!event.isCanceled()
                && livingEntity instanceof Wolf wolf
                && livingEntity.getType() == EntityType.WOLF
                && !wolf.level.isClientSide){
            Level level = wolf.level;
            level.getProfiler().push("wolfBrain");
            ((Brain<Wolf>)wolf.getBrain()).tick((ServerLevel)level, wolf);
            level.getProfiler().pop();
            level.getProfiler().push("wolfActivityUpdate");
            WolfAi.updateActivity(wolf);
            level.getProfiler().pop();
        }
    }

    @SubscribeEvent
    static void onLivingDamage(LivingDamageEvent event){
        LivingEntity livingEntity = event.getEntity();
        if(!event.isCanceled()
                && livingEntity instanceof Wolf wolf
                && livingEntity.getType() == EntityType.WOLF
                && event.getSource().getEntity() instanceof LivingEntity attacker){
            WolfAi.wasHurtBy(wolf, attacker);
        }
    }
}
