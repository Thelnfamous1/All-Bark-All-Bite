package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.behavior.*;
import com.infamous.call_of_the_wild.common.behavior.hunter.RememberIfHuntTargetWasKilled;
import com.infamous.call_of_the_wild.common.behavior.hunter.StartHunting;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.registry.COTWSensorTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.AngerAi;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

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
            //MemoryModuleType.HAS_HUNTING_COOLDOWN,
            MemoryModuleType.HUNTED_RECENTLY,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.IS_PANICKING,
            MemoryModuleType.IS_TEMPTED,
            MemoryModuleType.LOOK_TARGET,
            COTWMemoryModuleTypes.NEARBY_ADULTS.get(),
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.PATH,
            MemoryModuleType.TEMPTING_PLAYER,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
            MemoryModuleType.UNIVERSAL_ANGER,
            MemoryModuleType.WALK_TARGET
    );
    public static final Collection<? extends SensorType<? extends Sensor<? super Wolf>>> SENSOR_TYPES = ImmutableList.of(
            COTWSensorTypes.ANIMAL_TEMPTATIONS.get(),
            SensorType.HURT_BY,
            SensorType.NEAREST_ADULT,
            COTWSensorTypes.NEAREST_ADULTS.get(),
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_PLAYERS,
            COTWSensorTypes.WOLF_SPECIFIC_SENSOR.get()
    );
    public static final float WOLF_SIZE_SCALE = 1.25F;

    public static Brain<Wolf> makeBrain(Brain<Wolf> brain) {
        initCoreActivity(brain);
        initFightActivity(brain);
        initRetreatActivity(brain);
        initIdleActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Wolf> brain) {
        brain.addActivity(Activity.CORE, 0,
                ImmutableList.of(
                        new Swim(WolflikeAi.JUMP_CHANCE_IN_WATER),
                        new RunIf<>(WolflikeAi::shouldPanic, new AnimalPanic(WolflikeAi.SPEED_MODIFIER_PANICKING), true),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new SitWhenOrderedTo(),
                        new StopSittingToWalk(),
                        new OwnerHurtByTarget(),
                        new OwnerHurtTarget(),
                        new CopyMemoryWithExpiry<>(
                                WolflikeAi::isNearDisliked,
                                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                                MemoryModuleType.AVOID_TARGET,
                                WolflikeAi.AVOID_DURATION),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new StopBeingAngryIfTargetDead<>()));
    }

    private static void initFightActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0,
                ImmutableList.of(
                        new StopAttackingIfTargetInvalid<>(),
                        new SetWalkTargetFromAttackTargetIfTargetOutOfReach(WolflikeAi.SPEED_MODIFIER_CHASING),
                        new LeapAtTarget(),
                        new MeleeAttack(WolflikeAi.ATTACK_COOLDOWN_TICKS),
                        new RememberIfHuntTargetWasKilled<>(WolfAi::isHuntTarget),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)),
                MemoryModuleType.ATTACK_TARGET);
    }

    private static boolean isHuntTarget(Wolf wolf, LivingEntity target) {
        return AiUtil.isHuntTarget(wolf, target, COTWTags.WOLF_HUNT_TARGETS);
    }

    private static void initRetreatActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 0,
                ImmutableList.of(
                        SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, WolflikeAi.SPEED_MODIFIER_RETREATING, WolflikeAi.DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING, false),
                        createIdleLookBehaviors(),
                        createIdleMovementBehaviors(),
                        new EraseMemoryIf<>(WolfAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)),
                MemoryModuleType.AVOID_TARGET);
    }

    private static RunSometimes<Wolf> createIdleLookBehaviors() {
        return new RunSometimes<>(
                new SetEntityLookTarget(EntityType.PLAYER, WolflikeAi.MAX_LOOK_DIST),
                UniformInt.of(30, 60));
    }

    private static RunOne<Wolf> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(WolflikeAi.SPEED_MODIFIER_WALKING), 2),
                        Pair.of(new SetWalkTargetFromLookTarget(WolflikeAi.SPEED_MODIFIER_WALKING, 3), 2),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static boolean wantsToStopFleeing(TamableAnimal tamableAnimal) {
        Brain<?> brain = tamableAnimal.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        } else {
            LivingEntity avoidTarget = brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
            EntityType<?> avoidType = avoidTarget.getType();
            if (wantsToAvoid(avoidType)) {
                return !brain.isMemoryValue(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), avoidTarget);
            } else {
                return !tamableAnimal.isBaby();
            }
        }
    }

    private static boolean wantsToAvoid(EntityType<?> entityType) {
        return entityType.is(COTWTags.WOLF_DISLIKED);
    }

    private static void initIdleActivity(Brain<Wolf> brain) {
        brain.addActivity(Activity.IDLE, 0,
                ImmutableList.of(
                        new AnimalMakeLove(EntityType.WOLF, WolflikeAi.SPEED_MODIFIER_BREEDING),
                        new RunOne<>(
                                ImmutableList.of(
                                        Pair.of(new RunIf<>(WolfAi::canBeTempted, new FollowTemptation(WolflikeAi::getSpeedModifierTempted), true), 1),
                                        Pair.of(new BabyFollowAdult<>(WolflikeAi.ADULT_FOLLOW_RANGE, WolflikeAi.SPEED_MODIFIER_FOLLOWING_ADULT), 1))
                        ),
                        new Beg<>(WolfAi::isInteresting, Wolf::setIsInterested, WolflikeAi.MAX_LOOK_DIST),
                        createIdleLookBehaviors(),
                        createIdleMovementBehaviors(),
                        new StartAttacking<>(WolfAi::canAttack, WolflikeAi::findNearestValidAttackTarget),
                        new StartHunting<>(WolfAi::canHunt)));
    }

    private static boolean canHunt(Wolf wolf){
        return !wolf.isBaby() && canAttack(wolf);
    }

    private static boolean canBeTempted(Wolf wolf) {
        return wolf.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER).map(le -> le.isDiscrete() || wolf.isOwnedBy(le)).isPresent();
    }

    private static boolean canAttack(Wolf wolf) {
        return !BehaviorUtils.isBreeding(wolf);
    }

    public static boolean isInteresting(Wolf wolf, ItemStack stack) {
        return wolf.isFood(stack) || stack.is(COTWTags.WOLF_LOVED);
    }

    /**
     * Called by {@link com.infamous.call_of_the_wild.common.ForgeEventHandler#onLivingUpdate(LivingEvent.LivingTickEvent)}
     */
    public static void updateActivity(Wolf wolf) {
        Brain<?> brain = wolf.getBrain();
        Activity previous = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE));
        Activity current = brain.getActiveNonCoreActivity().orElse(null);

        if (previous != current) {
            getSoundForCurrentActivity(wolf).ifPresent(se -> wolf.playSound(getSoundForActivity(wolf, current), 0.4F, wolf.getVoicePitch()));
        }

        wolf.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        wolf.setSprinting(AiUtil.hasAnyMemory(wolf,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.AVOID_TARGET,
                MemoryModuleType.IS_PANICKING));
    }

    protected static Optional<SoundEvent> getSoundForCurrentActivity(Wolf wolf) {
        return wolf.getBrain().getActiveNonCoreActivity().map((a) -> getSoundForActivity(wolf, a));
    }

    private static SoundEvent getSoundForActivity(Wolf wolf, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.WOLF_GROWL;
        } else if (activity == Activity.AVOID && GenericAi.isNearAvoidTarget(wolf, WolflikeAi.DESIRED_DISTANCE_FROM_DISLIKED)) {
            return SoundEvents.WOLF_HURT;
        } else if (wolf.getRandom().nextInt(3) == 0) {
            return wolf.isTame() && wolf.getHealth() < wolf.getMaxHealth() * 0.5F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundEvents.WOLF_AMBIENT;
        }
    }

    /**
     * Called by {@link com.infamous.call_of_the_wild.common.ForgeEventHandler#onLivingDamage(LivingDamageEvent)}
     */
    public static void wasHurtBy(Wolf wolf, LivingEntity attacker) {

        AiUtil.eraseAllMemories(wolf,
                MemoryModuleType.BREED_TARGET);

        if (wolf.isBaby()) {
            GenericAi.setAvoidTarget(wolf, attacker, WolflikeAi.RETREAT_DURATION.sample(wolf.level.random));
            if (Sensor.isEntityAttackableIgnoringLineOfSight(wolf, attacker)) {
                AngerAi.broadcastAngerTarget(GenericAi.getNearbyAdults(wolf).stream().map(Wolf.class::cast).filter(w -> WolflikeAi.wantsToRetaliate(w, attacker)).toList(), attacker, WolflikeAi.ANGER_DURATION.sample(wolf.getRandom()));
            }
        } else if(!wolf.getBrain().isActive(Activity.AVOID)){
            AngerAi.maybeRetaliate(wolf, GenericAi.getNearbyAdults(wolf).stream().map(Wolf.class::cast).filter(w -> WolflikeAi.wantsToRetaliate(w, attacker)).toList(), attacker, WolflikeAi.ANGER_DURATION.sample(wolf.getRandom()), 4.0D);
        }
    }
}
