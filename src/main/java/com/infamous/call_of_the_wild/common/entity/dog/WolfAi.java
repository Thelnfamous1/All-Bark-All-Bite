package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.behavior.*;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtByTarget;
import com.infamous.call_of_the_wild.common.behavior.hunter.RememberIfHuntTargetWasKilled;
import com.infamous.call_of_the_wild.common.behavior.hunter.StartHunting;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtTarget;
import com.infamous.call_of_the_wild.common.behavior.pet.SitWhenOrderedTo;
import com.infamous.call_of_the_wild.common.behavior.pet.StopSittingToWalk;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.registry.COTWSensorTypes;
import com.infamous.call_of_the_wild.common.util.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public class WolfAi {

    public static final Collection<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.AVOID_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            //MemoryModuleType.HAS_HUNTING_COOLDOWN,
            COTWMemoryModuleTypes.HOWLED_RECENTLY.get(),
            MemoryModuleType.HUNTED_RECENTLY,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.IS_PANICKING,
            MemoryModuleType.IS_TEMPTED,
            MemoryModuleType.LOOK_TARGET,
            COTWMemoryModuleTypes.NEARBY_ADULTS.get(),
            COTWMemoryModuleTypes.NEARBY_BABIES.get(),
            COTWMemoryModuleTypes.NEARBY_KIN.get(),
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            COTWMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(),
            COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
            COTWMemoryModuleTypes.NEAREST_VISIBLE_KIN.get(),
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            COTWMemoryModuleTypes.PACK_LEADER.get(),
            COTWMemoryModuleTypes.PACK_SIZE.get(),
            MemoryModuleType.PATH,
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
    private static final Predicate<Entity> NOT_DISCRETE_NOT_CREATIVE_OR_SPECTATOR = (e) -> !e.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e);
    private static final int HOWL_VOLUME = 4;

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
                        new EraseMemoryIf<>(WolfAi::wantsToStopBeingTempted, MemoryModuleType.TEMPTING_PLAYER),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new Retaliate<>(WolfAi::wasHurtBy),
                        new StopBeingAngryIfTargetDead<>()));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static boolean wantsToStopBeingTempted(Wolf wolf) {
        Brain<?> brain = wolf.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.TEMPTING_PLAYER)) {
            return true;
        } else {
            Player player = brain.getMemory(MemoryModuleType.TEMPTING_PLAYER).get();
            return wantsToAvoidPlayer(wolf, player);
        }
    }

    public static boolean wantsToAvoidPlayer(Wolf wolf, Player player) {
        return !wolf.isOwnedBy(player) && NOT_DISCRETE_NOT_CREATIVE_OR_SPECTATOR.test(player);
    }

    private static void wasHurtBy(Wolf wolf, LivingEntity attacker) {
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

    private static void initFightActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0,
                ImmutableList.of(
                        new StopAttackingIfTargetInvalid<>(),
                        new SetWalkTargetFromAttackTargetIfTargetOutOfReach(WolflikeAi.SPEED_MODIFIER_CHASING),
                        new JumpAtTarget(),
                        new MeleeAttack(WolflikeAi.ATTACK_COOLDOWN_TICKS),
                        new RememberIfHuntTargetWasKilled<>(WolfAi::isHuntTarget, WolflikeAi.TIME_BETWEEN_HUNTS),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)),
                MemoryModuleType.ATTACK_TARGET);
    }

    private static boolean isHuntTarget(Wolf wolf, LivingEntity target) {
        return AiUtil.isHuntTarget(wolf, target, COTWTags.WOLF_HUNT_TARGETS);
    }

    private static void initRetreatActivity(Brain<Wolf> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 0,
                ImmutableList.of(
                        SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, WolflikeAi.SPEED_MODIFIER_RETREATING, WolflikeAi.DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING, true),
                        createIdleLookBehaviors(),
                        createIdleMovementBehaviors(),
                        new EraseMemoryIf<>(WolfAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)),
                MemoryModuleType.AVOID_TARGET);
    }

    private static RunOne<Wolf> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(EntityType.WOLF, WolflikeAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(WolflikeAi.MAX_LOOK_DIST), 1),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<Wolf> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(WolflikeAi.SPEED_MODIFIER_WALKING), 2),
                        Pair.of(InteractWith.of(EntityType.WOLF, WolflikeAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, WolflikeAi.SPEED_MODIFIER_WALKING, 2), 2),
                        Pair.of(new RunIf<>(GenericAi::doesntSeeAnyPlayerHoldingWantedItem, new SetWalkTargetFromLookTarget(WolflikeAi.SPEED_MODIFIER_WALKING, 3)), 2),
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
                return false;
            }
        }
    }

    private static boolean wantsToAvoid(EntityType<?> entityType) {
        return entityType.is(COTWTags.WOLF_DISLIKED);
    }

    private static void initIdleActivity(Brain<Wolf> brain) {
        brain.addActivity(Activity.IDLE, 0,
                ImmutableList.of(
                        new Howl<>(WolfAi::canAlert, WolfAi::onHowlStarted, WolfAi::getAlertableSpeedModifier, WolflikeAi.STOP_FOLLOW_DISTANCE, WolflikeAi.TIME_BETWEEN_HOWLS),
                        new AnimalMakeLove(EntityType.WOLF, WolflikeAi.SPEED_MODIFIER_BREEDING),
                        new FollowTemptation(WolflikeAi::getSpeedModifierTempted),
                        new FollowPackLeader<>(WolflikeAi.ADULT_FOLLOW_RANGE, WolflikeAi::getMaxPackSize, WolflikeAi.SPEED_MODIFIER_FOLLOWING_ADULT),
                        new BabyFollowAdult<>(WolflikeAi.ADULT_FOLLOW_RANGE, WolflikeAi.SPEED_MODIFIER_FOLLOWING_ADULT),
                        new Beg<>(WolfAi::isInteresting, Wolf::setIsInterested, WolflikeAi.MAX_LOOK_DIST),
                        new StartAttacking<>(WolfAi::canAttack, WolflikeAi::findNearestValidAttackTarget),
                        new StartHunting<>(WolfAi::canHunt, WolflikeAi.TIME_BETWEEN_HUNTS),
                        createIdleLookBehaviors(),
                        createIdleMovementBehaviors()));
    }

    private static boolean canAlert(Wolf wolf, LivingEntity other){
        if(wolf.getType() == other.getType()){
            return !AiUtil.hasAnyMemory(other,
                    MemoryModuleType.ATTACK_TARGET,
                    MemoryModuleType.AVOID_TARGET,
                    MemoryModuleType.IS_PANICKING,
                    MemoryModuleType.TEMPTING_PLAYER)
                    && PackAi.canLead(other, wolf, WolflikeAi.getMaxPackSize(wolf));
        }
        return false;
    }

    private static void onHowlStarted(Wolf wolf){
        GenericAi.stopWalking(wolf);
        wolf.playSound(SoundEvents.WOLF_HOWL, HOWL_VOLUME, wolf.getVoicePitch());
        wolf.gameEvent(GameEvent.ENTITY_ROAR);
    }

    private static float getAlertableSpeedModifier(LivingEntity livingEntity){
        return WolflikeAi.SPEED_MODIFIER_WALKING;
    }

    public static boolean isInteresting(Wolf wolf, ItemStack stack) {
        return wolf.isFood(stack) || stack.is(COTWTags.WOLF_LOVED);
    }

    private static boolean canAttack(Wolf wolf) {
        return !BehaviorUtils.isBreeding(wolf);
    }

    private static boolean canHunt(Wolf wolf){
        return !wolf.isBaby() && canAttack(wolf);
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

        PackAi.updatePack(wolf, FollowPackLeader.INTERVAL_TICKS);
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
        return WolflikeAi.isDisliked(wolf, livingEntity, COTWTags.WOLF_DISLIKED)
                || livingEntity instanceof Player player && wantsToAvoidPlayer(wolf, player);
    }
}
