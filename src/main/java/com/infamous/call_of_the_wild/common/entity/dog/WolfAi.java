package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.behavior.*;
import com.infamous.call_of_the_wild.common.registry.COTWActivities;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.registry.COTWSensorTypes;
import com.infamous.call_of_the_wild.common.util.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.Collection;
import java.util.Optional;

public class WolfAi {

    public static final Collection<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            COTWMemoryModuleTypes.IS_ALERT.get(),
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.AVOID_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            //MemoryModuleType.HAS_HUNTING_COOLDOWN,
            COTWMemoryModuleTypes.HAS_SHELTER.get(),
            COTWMemoryModuleTypes.HOWLED_RECENTLY.get(),
            MemoryModuleType.HUNTED_RECENTLY,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.IS_PANICKING,
            COTWMemoryModuleTypes.IS_SLEEPING.get(),
            MemoryModuleType.IS_TEMPTED,
            MemoryModuleType.LAST_SLEPT,
            MemoryModuleType.LAST_WOKEN,
            MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
            MemoryModuleType.LONG_JUMP_MID_JUMP,
            COTWMemoryModuleTypes.LONG_JUMP_TARGET.get(),
            MemoryModuleType.LOOK_TARGET,
            COTWMemoryModuleTypes.NEAREST_ADULTS.get(),
            COTWMemoryModuleTypes.NEAREST_BABIES.get(),
            COTWMemoryModuleTypes.NEAREST_ALLIES.get(),
            MemoryModuleType.NEAREST_ATTACKABLE,
            COTWMemoryModuleTypes.NEAREST_HUNTABLE.get(),
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            COTWMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(),
            COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
            COTWMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(),
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            COTWMemoryModuleTypes.PACK_LEADER.get(),
            COTWMemoryModuleTypes.PACK_SIZE.get(),
            MemoryModuleType.PATH,
            COTWMemoryModuleTypes.POUNCE_DELAY.get(),
            COTWMemoryModuleTypes.STALK_TARGET.get(),
            MemoryModuleType.TEMPTING_PLAYER,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
            MemoryModuleType.UNIVERSAL_ANGER,
            MemoryModuleType.WALK_TARGET
    );
    public static final Collection<? extends SensorType<? extends Sensor<? super Wolf>>> SENSOR_TYPES = ImmutableList.of(
            COTWSensorTypes.ANIMAL_TEMPTATIONS.get(),
            SensorType.HURT_BY,

            // dependent on NEAREST_VISIBLE_LIVING_ENTITIES
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_ADULT,
            COTWSensorTypes.NEAREST_KIN.get(),

            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_PLAYERS,
            COTWSensorTypes.WOLF_SPECIFIC_SENSOR.get()
    );
    public static final float WOLF_SIZE_SCALE = 1.25F;
    public static final float WOLF_SIZE_LONG_JUMPING_SCALE = 0.7F;

    public static Brain<Wolf> makeBrain(Brain<Wolf> brain) {
        initCoreActivity(brain);
        initFightActivity(brain);
        initRetreatActivity(brain);
        initLongJumpActivity(brain);
        initStalkActivity(brain);
        initRestActivity(brain);
        initIdleActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Wolf> brain) {
        brain.addActivity(Activity.CORE, WolfGoalPackages.getCorePackage());
    }

    private static void initFightActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.FIGHT,
                WolfGoalPackages.getFightPackage(),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT),
                        Pair.of(COTWMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(MemoryModuleType.ATTACK_TARGET));
    }

    private static void initRetreatActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.AVOID,
                WolfGoalPackages.getAvoidPackage(),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.AVOID_TARGET, MemoryStatus.VALUE_PRESENT),
                        Pair.of(COTWMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(MemoryModuleType.AVOID_TARGET));
    }

    private static void initStalkActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(COTWActivities.STALK.get(),
                WolfGoalPackages.getStalkPackage(),
                ImmutableSet.of(
                        Pair.of(COTWMemoryModuleTypes.STALK_TARGET.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT),
                        Pair.of(COTWMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(
                        COTWMemoryModuleTypes.STALK_TARGET.get()
                )
        );
    }

    private static void initRestActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.REST,
                0,
                ImmutableList.of(),
                COTWMemoryModuleTypes.IS_SLEEPING.get()
        );
    }

    private static void initIdleActivity(Brain<Wolf> brain) {
        brain.addActivityWithConditions(Activity.IDLE,
                WolfGoalPackages.getIdlePackage(),
                ImmutableSet.of(
                        Pair.of(COTWMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT)
                )
        );
    }

    private static void initLongJumpActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.LONG_JUMP,
                WolfGoalPackages.getLongJumpPackage(),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(
                )
        );
    }

    /**
     * Called by {@link com.infamous.call_of_the_wild.common.ForgeEventHandler#onLivingUpdate(LivingEvent.LivingTickEvent)}
     */
    public static void updateActivity(Wolf wolf) {
        Brain<?> brain = wolf.getBrain();
        Activity previous = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, COTWActivities.STALK.get(), Activity.REST, Activity.IDLE, Activity.LONG_JUMP));
        Activity current = brain.getActiveNonCoreActivity().orElse(null);

        if (previous != current) {
            getSoundForCurrentActivity(wolf).ifPresent(se -> wolf.playSound(getSoundForActivity(wolf, current), 0.4F, wolf.getVoicePitch()));
        }

        wolf.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        wolf.setSprinting(canSprint(wolf));

        PackAi.updatePack(wolf, FollowPackLeader.INTERVAL_TICKS);

        /*
        Optional<LivingEntity> target = HunterAi.getStalkTarget(wolf);
        if (target.isEmpty() || !target.get().isAlive()) {
            if(wolf.hasPose(Pose.CROUCHING)){
                wolf.setPose(Pose.STANDING);
                wolf.setIsInterested(false);
            }
        }
         */
        boolean inWater = wolf.isInWater();
        if (inWater || wolf.getTarget() != null || wolf.level.isThundering()) {
            GenericAi.wakeUp(wolf);
        }

        if (inWater || wolf.isSleeping()) {
            wolf.setOrderedToSit(false);
            wolf.setInSittingPose(false);
        }

        if(wolf.isSleeping()){
            wolf.setJumping(false);
            wolf.xxa = 0.0F;
            wolf.zza = 0.0F;
        }
    }

    private static boolean canSprint(Wolf wolf) {
        return AiUtil.hasAnyMemory(wolf,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.AVOID_TARGET,
                MemoryModuleType.IS_PANICKING
        ) && !AiUtil.hasAnyMemory(wolf,
                COTWMemoryModuleTypes.LONG_JUMP_TARGET.get(),
                MemoryModuleType.LONG_JUMP_MID_JUMP
        );
    }

    protected static Optional<SoundEvent> getSoundForCurrentActivity(Wolf wolf) {
        return wolf.getBrain().getActiveNonCoreActivity().filter(WolfAi::activityMakesSound).map((a) -> getSoundForActivity(wolf, a));
    }

    private static boolean activityMakesSound(Activity activity) {
        return activity != COTWActivities.STALK.get();
    }

    private static SoundEvent getSoundForActivity(Wolf wolf, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.WOLF_GROWL;
        } else if (activity == Activity.AVOID && GenericAi.isNearAvoidTarget(wolf, SharedWolfAi.DESIRED_DISTANCE_FROM_DISLIKED)) {
            return SoundEvents.WOLF_HURT;
        } else if (activity == Activity.REST) {
            return SoundEvents.FOX_SLEEP;
        } else if (wolf.getRandom().nextInt(3) == 0) {
            return wolf.isTame() && wolf.getHealth() < wolf.getMaxHealth() * 0.5F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundEvents.WOLF_AMBIENT;
        }
    }


    /**
     * Called by {@link com.infamous.call_of_the_wild.common.ForgeEventHandler#onLivingUpdate(LivingEvent.LivingTickEvent)}
     */
    public static void updateAi(ServerLevel serverLevel, Wolf wolf) {
        serverLevel.getProfiler().push("wolfBrain");
        BrainUtil.getTypedBrain(wolf).tick(serverLevel, wolf);
        serverLevel.getProfiler().pop();
        serverLevel.getProfiler().push("wolfActivityUpdate");
        updateActivity(wolf);
        serverLevel.getProfiler().pop();
    }

    public static boolean isDisliked(Wolf wolf, LivingEntity livingEntity) {
        return SharedWolfAi.isDisliked(wolf, livingEntity, COTWTags.WOLF_DISLIKED)
                || livingEntity instanceof Player player && WolfGoalPackages.wantsToAvoidPlayer(wolf, player);
    }

    public static void initMemories(Wolf wolf, RandomSource random) {
        wolf.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, WolfGoalPackages.TIME_BETWEEN_LONG_JUMPS.sample(random));
        SharedWolfAi.initMemories(wolf, random);
    }
}
