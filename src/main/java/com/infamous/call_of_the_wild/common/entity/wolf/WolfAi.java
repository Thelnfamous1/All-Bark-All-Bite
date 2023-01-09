package com.infamous.call_of_the_wild.common.entity.wolf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.entity.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.registry.ABABSensorTypes;
import com.infamous.call_of_the_wild.common.util.*;
import com.infamous.call_of_the_wild.common.entity.AnimalAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class WolfAi {
    public static final Collection<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.AVOID_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            ABABMemoryModuleTypes.FOLLOWERS.get(),
            ABABMemoryModuleTypes.HOWL_LOCATION.get(),
            ABABMemoryModuleTypes.HOWLED_RECENTLY.get(),
            MemoryModuleType.HUNTED_RECENTLY,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.IS_PANICKING,
            ABABMemoryModuleTypes.IS_SLEEPING.get(),
            ABABMemoryModuleTypes.IS_STALKING.get(),
            MemoryModuleType.LAST_SLEPT,
            MemoryModuleType.LAST_WOKEN,
            ABABMemoryModuleTypes.LEADER.get(),
            MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
            MemoryModuleType.LONG_JUMP_MID_JUMP,
            ABABMemoryModuleTypes.LONG_JUMP_TARGET.get(),
            MemoryModuleType.LOOK_TARGET,
            ABABMemoryModuleTypes.NEAREST_ADULTS.get(),
            ABABMemoryModuleTypes.NEAREST_BABIES.get(),
            ABABMemoryModuleTypes.NEAREST_ALLIES.get(),
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            ABABMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            ABABMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(),
            ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
            ABABMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(),
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.PATH,
            MemoryModuleType.UNIVERSAL_ANGER,
            MemoryModuleType.WALK_TARGET,
            ABABMemoryModuleTypes.WOLF_VIBRATION_LISTENER.get()
    );
    public static final Collection<? extends SensorType<? extends Sensor<? super Wolf>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.HURT_BY,

            // dependent on NEAREST_VISIBLE_LIVING_ENTITIES
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_ADULT,
            ABABSensorTypes.NEAREST_ALLIES.get(),

            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_PLAYERS,
            ABABSensorTypes.WOLF_SPECIFIC_SENSOR.get(),
            ABABSensorTypes.WOLF_VIBRATION_SENSOR.get()
    );
    public static final float WOLF_SIZE_SCALE = 1.25F;
    public static final float WOLF_SIZE_LONG_JUMPING_SCALE = 0.7F;

    public static Brain<Wolf> makeBrain(Brain<Wolf> brain) {
        initCoreActivity(brain);
        initFightActivity(brain);
        initRetreatActivity(brain);
        initLongJumpActivity(brain);
        initMeetActivity(brain);
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
                        Pair.of(ABABMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of()); // don't erase the attack target if we are simply in the middle of a pounce
    }

    private static void initRetreatActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.AVOID,
                WolfGoalPackages.getAvoidPackage(),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.AVOID_TARGET, MemoryStatus.VALUE_PRESENT),
                        Pair.of(ABABMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(MemoryModuleType.AVOID_TARGET));
    }

    private static void initLongJumpActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.LONG_JUMP,
                WolfGoalPackages.getLongJumpPackage(),
                ImmutableSet.of(
                        Pair.of(ABABMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_PRESENT)
                        //Pair.of(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(
                        ABABMemoryModuleTypes.LONG_JUMP_TARGET.get()
                )
        );
    }

    private static void initMeetActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.MEET,
                WolfGoalPackages.getMeetPackage(),
                ImmutableSet.of(
                        Pair.of(ABABMemoryModuleTypes.HOWL_LOCATION.get(), MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT)
                ),
                ImmutableSet.of(
                        ABABMemoryModuleTypes.HOWL_LOCATION.get()
                )
        );
    }

    private static void initRestActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.REST,
                WolfGoalPackages.getRestPackage(),
                ImmutableSet.of(
                        Pair.of(ABABMemoryModuleTypes.IS_SLEEPING.get(), MemoryStatus.VALUE_PRESENT)
                ),
                ImmutableSet.of(
                        ABABMemoryModuleTypes.IS_SLEEPING.get()
                )
        );
    }

    private static void initIdleActivity(Brain<Wolf> brain) {
        brain.addActivity(Activity.IDLE,
                WolfGoalPackages.getIdlePackage()
        );
    }

    private static void updateActivity(Wolf wolf) {
        Brain<?> brain = wolf.getBrain();
        Activity previous = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(
                Activity.FIGHT, Activity.AVOID, Activity.LONG_JUMP, Activity.MEET, Activity.REST, Activity.IDLE));
        Activity current = brain.getActiveNonCoreActivity().orElse(null);

        if (previous != current) {
            getSoundForCurrentActivity(wolf).ifPresent(se -> AiUtil.playSoundEvent(wolf, se, AiUtil.getSoundVolume(wolf)));
        }

        if(GenericAi.getAttackTarget(wolf).isEmpty() && wolf.getTarget() != null) wolf.setTarget(null);

        wolf.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        wolf.setSprinting(canSprint(wolf));

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

        if(PackAi.hasFollowers(wolf)){
            if(wolf.tickCount % 20 == 0){
                MiscUtil.sendParticlesAroundSelf((ServerLevel) wolf.level, wolf, ParticleTypes.ANGRY_VILLAGER, wolf.getEyeHeight(),  10, 0.2D);
            }
        } else if(PackAi.isFollower(wolf)){
            if(wolf.tickCount % 20 == 0){
                MiscUtil.sendParticlesAroundSelf((ServerLevel) wolf.level, wolf, ParticleTypes.HAPPY_VILLAGER, wolf.getEyeHeight(),  10, 0.2D);
            }
        }
    }

    private static boolean canSprint(Wolf wolf) {
        return AiUtil.hasAnyMemory(wolf,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.AVOID_TARGET,
                MemoryModuleType.IS_PANICKING
        ) && !AiUtil.hasAnyMemory(wolf,
                ABABMemoryModuleTypes.IS_STALKING.get(),
                ABABMemoryModuleTypes.LONG_JUMP_TARGET.get(),
                MemoryModuleType.LONG_JUMP_MID_JUMP
        );
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Wolf wolf) {
        return wolf.getBrain().getActiveNonCoreActivity().map((a) -> getSoundForActivity(wolf, a));
    }

    @Nullable
    private static SoundEvent getSoundForActivity(Wolf wolf, Activity activity) {
        if (activity == Activity.FIGHT) {
            return !isStalkingOrWantsToStalk(wolf) ? SoundEvents.WOLF_GROWL : null;
        } else if (activity == Activity.AVOID) {
            return GenericAi.isNearAvoidTarget(wolf, SharedWolfAi.DESIRED_DISTANCE_FROM_DISLIKED) ? SoundEvents.WOLF_HURT : null;
        } else if (activity == Activity.REST) {
            return SoundEvents.FOX_SLEEP;
        } else if (wolf.getRandom().nextInt(3) == 0) {
            return wolf.isTame() && wolf.getHealth() < wolf.getMaxHealth() * 0.5F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundEvents.WOLF_AMBIENT;
        }
    }

    private static boolean isStalkingOrWantsToStalk(Wolf wolf) {
        Optional<LivingEntity> attackTarget = GenericAi.getAttackTarget(wolf);
        return wolf.getBrain().hasMemoryValue(ABABMemoryModuleTypes.IS_STALKING.get())
                || attackTarget.isPresent()
                && WolfGoalPackages.wantsToStalk(wolf, attackTarget.get())
                && wolf.distanceToSqr(attackTarget.get()) > SharedWolfAi.POUNCE_DISTANCE;
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
        return SharedWolfAi.isDisliked(livingEntity, ABABTags.WOLF_DISLIKED)
                || isDislikedPlayer(wolf, livingEntity);
    }

    public static boolean isDislikedPlayer(Wolf wolf, LivingEntity livingEntity) {
        return livingEntity instanceof Player player
                && !wolf.isOwnedBy(player)
                && !player.isDiscrete()
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player);
    }

    public static void initMemories(Wolf wolf, RandomSource random) {
        wolf.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, WolfGoalPackages.TIME_BETWEEN_LONG_JUMPS.sample(random));
        SharedWolfAi.initMemories(wolf, random);
    }

    public static InteractionResult mobInteract(Wolf wolf, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        Item item = itemInHand.getItem();
        if (!wolf.level.isClientSide) {
            if (wolf.isTame() && wolf.isOwnedBy(player)) {
                if (wolf.isFood(itemInHand) && AiUtil.isInjured(wolf)) {
                    AiUtil.animalEat(wolf, itemInHand, AiUtil.getSoundVolume(wolf));
                    AnimalAccessor.cast(wolf).takeItemFromPlayer(player, hand, itemInHand);
                    return InteractionResult.SUCCESS;
                }

                if (!(item instanceof DyeItem dyeItem)) {
                    ItemStack copy = itemInHand.copy(); // retain a copy of the item before it is potentially consumed during animalInteract
                    InteractionResult animalInteractionResult = AnimalAccessor.cast(wolf).animalInteract(player, hand);
                    if(animalInteractionResult.consumesAction()){
                        AiUtil.animalEat(wolf, copy, AiUtil.getSoundVolume(wolf));
                    }
                    return animalInteractionResult;
                }

                DyeColor dyeColor = dyeItem.getDyeColor();
                if (dyeColor != wolf.getCollarColor()) {
                    wolf.setCollarColor(dyeColor);
                    AnimalAccessor.cast(wolf).takeItemFromPlayer(player, hand, itemInHand);

                    return InteractionResult.SUCCESS;
                }
            }
        }
        return AnimalAccessor.cast(wolf).animalInteract(player, hand);
    }
}
