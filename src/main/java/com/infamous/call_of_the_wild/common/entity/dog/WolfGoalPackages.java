package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.behavior.*;
import com.infamous.call_of_the_wild.common.behavior.hunter.*;
import com.infamous.call_of_the_wild.common.behavior.long_jump.LongJumpToTarget;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtByTarget;
import com.infamous.call_of_the_wild.common.behavior.pet.SitWhenOrderedTo;
import com.infamous.call_of_the_wild.common.behavior.pet.StopSittingToWalk;
import com.infamous.call_of_the_wild.common.behavior.PerchAndSearch;
import com.infamous.call_of_the_wild.common.behavior.sleep.SeekShelter;
import com.infamous.call_of_the_wild.common.behavior.sleep.StartSleeping;
import com.infamous.call_of_the_wild.common.behavior.sleep.WakeUpTrigger;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public class WolfGoalPackages {
    public static final int CATCH_UP_DISTANCE = 8;
    private static final int HOWL_VOLUME = 4;
    public static final float MAX_JUMP_VELOCITY = 1.5F;
    public static final float SPEED_MODIFIER_STALKING = 0.6F;
    private static final Predicate<Entity> NOT_DISCRETE_NOT_CREATIVE_OR_SPECTATOR = (e) -> !e.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e);
    static final UniformInt TIME_BETWEEN_LONG_JUMPS = TimeUtil.rangeOfSeconds(0, 1);

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getCorePackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new HurtByTrigger<>(WolfGoalPackages::wasHurtBy),
                        new WakeUpTrigger<>(WolfGoalPackages::wantsToWakeUp),
                        new EraseMemoryIf<>(WolfGoalPackages::isNotSleeping, COTWMemoryModuleTypes.IS_SLEEPING.get()),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS),

                        new Swim(SharedWolfAi.JUMP_CHANCE_IN_WATER),
                        new RunIf<>(SharedWolfAi::shouldPanic, new AnimalPanic(SharedWolfAi.SPEED_MODIFIER_PANICKING), true),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),

                        new SitWhenOrderedTo(),
                        new StopSittingToWalk(),
                        new OwnerHurtByTarget(),
                        //new OwnerHurtTarget(),
                        new CopyMemoryWithExpiry<>(
                                SharedWolfAi::isNearDisliked,
                                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                                MemoryModuleType.AVOID_TARGET,
                                SharedWolfAi.AVOID_DURATION),
                        new EraseMemoryIf<>(WolfGoalPackages::wantsToStopBeingTempted, MemoryModuleType.TEMPTING_PLAYER),
                        new StopBeingAngryIfTargetDead<>()));
    }

    private static boolean isNotSleeping(Wolf wolf){
        return !wolf.isSleeping();
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

    private static boolean wantsToWakeUp(Wolf wolf){
        Optional<BlockPos> sleepingPos = wolf.getSleepingPos();
        if (sleepingPos.isEmpty()) {
            return true;
        } else {
            return !wolf.getBrain().isActive(Activity.REST)
                    || !sleepingPos.get().closerToCenterThan(wolf.position(), 1.14D)
                    || !SharedWolfAi.canSleep(wolf)
                    || AiUtil.hasAnyMemory(wolf, MemoryModuleType.BREED_TARGET);
        }
    }

    public static boolean wantsToAvoidPlayer(Wolf wolf, Player player) {
        return !wolf.isOwnedBy(player) && NOT_DISCRETE_NOT_CREATIVE_OR_SPECTATOR.test(player);
    }

    private static void wasHurtBy(Wolf wolf, LivingEntity attacker) {
        wolf.setIsInterested(false);
        SharedWolfAi.clearStates(wolf);

        AiUtil.eraseAllMemories(wolf,
                COTWMemoryModuleTypes.STALK_TARGET.get(),
                MemoryModuleType.BREED_TARGET);

        SharedWolfAi.tellAlliesIWasAttacked(wolf, attacker);
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getFightPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new StopAttackingIfTargetInvalid<>(),
                        new SetWalkTargetFromAttackTargetIfTargetOutOfReach(SharedWolfAi.SPEED_MODIFIER_CHASING),
                        new LeapAtTarget(SharedWolfAi.LEAP_YD, SharedWolfAi.TOO_CLOSE_TO_LEAP, SharedWolfAi.POUNCE_DISTANCE),
                        new MeleeAttack(SharedWolfAi.ATTACK_COOLDOWN_TICKS),
                        new RememberIfHuntTargetWasKilled<>(WolfGoalPackages::isHuntTarget, SharedWolfAi.TIME_BETWEEN_HUNTS),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)));
    }

    private static boolean isHuntTarget(Wolf wolf, LivingEntity target) {
        return AiUtil.isHuntTarget(wolf, target, COTWTags.WOLF_HUNT_TARGETS);
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getAvoidPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, SharedWolfAi.SPEED_MODIFIER_RETREATING, SharedWolfAi.DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING, true),
                        createIdleLookBehaviors(),
                        createIdleMovementBehaviors(),
                        new EraseMemoryIf<>(WolfGoalPackages::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)));
    }

    static RunOne<Wolf> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(EntityType.WOLF, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    static RunOne<Wolf> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(SharedWolfAi.SPEED_MODIFIER_WALKING), 2),
                        Pair.of(InteractWith.of(EntityType.WOLF, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(new RunIf<>(GenericAi::doesntSeeAnyPlayerHoldingWantedItem, new SetWalkTargetFromLookTarget(SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_LOOK_TARGET)), 2),
                        Pair.of(new RunIf<>(GenericAi::doesntSeeAnyPlayerHoldingWantedItem, new PerchAndSearch<>(Wolf::isInSittingPose, Wolf::setInSittingPose), true), 2),
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

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getLongJumpPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new LongJumpMidJump(TIME_BETWEEN_LONG_JUMPS, SoundEvents.WOLF_STEP),
                        new LongJumpToTarget<>(TIME_BETWEEN_LONG_JUMPS, MAX_JUMP_VELOCITY,
                                wolf -> SoundEvents.WOLF_STEP))
        );
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getStalkPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new StopStalkingIfTargetInvalid<>(WolfGoalPackages::onStalkTargetErased),
                        new StalkPrey<>(WolfGoalPackages::wantsToStalk, SPEED_MODIFIER_STALKING, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.POUNCE_DISTANCE, CATCH_UP_DISTANCE, Wolf::isInterested, Wolf::setIsInterested, MAX_JUMP_VELOCITY),
                        new Pounce<>(Wolf::setIsInterested, MAX_JUMP_VELOCITY)
                )
        );
    }

    private static void onStalkTargetErased(Wolf wolf, LivingEntity target) {
        wolf.setIsInterested(false);
        if (wolf.hasPose(Pose.CROUCHING)) {
            wolf.setPose(Pose.STANDING);
            wolf.getBrain().eraseMemory(COTWMemoryModuleTypes.POUNCE_DELAY.get());
        }
    }

    static boolean wantsToStalk(Wolf wolf, LivingEntity target){
        return !wolf.isBaby()
                && SharedWolfAi.canStartAttacking(wolf)
                && GenericAi.getNearbyVisibleAdults(wolf).isEmpty();
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getIdlePackage() {
        return BrainUtil.createPriorityPairs(0, ImmutableList.of(

                new AnimalMakeLove(EntityType.WOLF, SharedWolfAi.SPEED_MODIFIER_BREEDING),
                new StartStalking<>(WolfGoalPackages::wantsToStalk, SharedWolfAi.POUNCE_DISTANCE, Wolf::isInterested, SharedWolfAi.TIME_BETWEEN_HUNTS),
                new StartAttacking<>(SharedWolfAi::canStartAttacking, SharedWolfAi::findNearestValidAttackTarget),
                new StartHunting<>(WolfGoalPackages::canHunt, SharedWolfAi::startHunting, SharedWolfAi.TIME_BETWEEN_HUNTS),

                BrainUtil.gateBehaviors(
                        ImmutableMap.of(
                                MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT,
                                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                                COTWMemoryModuleTypes.STALK_TARGET.get(), MemoryStatus.VALUE_ABSENT,
                                MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_ABSENT,
                                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
                        ),
                        ImmutableSet.of(),
                        GateBehavior.OrderPolicy.ORDERED,
                        GateBehavior.RunningPolicy.RUN_ONE,
                        ImmutableList.of(
                                Pair.of(new SeekShelter<>(SharedWolfAi.SPEED_MODIFIER_WALKING), 1),
                                Pair.of(new StartSleeping<>(SharedWolfAi::canSleep), 1),
                                Pair.of(new HowlForAllies<>(
                                        WolfGoalPackages::wantsToHowl,
                                        WolfGoalPackages::wantsToListen,
                                        WolfGoalPackages::onHowlStarted,
                                        WolfGoalPackages::getListenerSpeedModifier,
                                        SharedWolfAi.STOP_FOLLOW_DISTANCE,
                                        SharedWolfAi.TIME_BETWEEN_HOWLS),
                                        1),
                                Pair.of(new FollowTemptation(SharedWolfAi::getSpeedModifierTempted), 1),
                                Pair.of(new FollowPackLeader<>(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi::getMaxPackSize, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT), 1),
                                Pair.of(new BabyFollowAdult<>(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT), 1),
                                Pair.of(new Beg<>(WolfGoalPackages::isInteresting, Wolf::setIsInterested, SharedWolfAi.MAX_LOOK_DIST), 1)
                        )),

                // Idle
                createIdleLookBehaviors(),
                createIdleMovementBehaviors()));
    }

    private static boolean wantsToHowl(Wolf wolf){
        return !AiUtil.hasAnyMemory(wolf, COTWMemoryModuleTypes.IS_SLEEPING.get());
    }

    private static boolean wantsToListen(Wolf wolf, LivingEntity other){
        if(wolf.getType() == other.getType()){
            return !AiUtil.hasAnyMemory(other,
                    MemoryModuleType.ATTACK_TARGET,
                    COTWMemoryModuleTypes.STALK_TARGET.get(),
                    MemoryModuleType.AVOID_TARGET,
                    MemoryModuleType.IS_PANICKING,
                    MemoryModuleType.TEMPTING_PLAYER)
                    && AiUtil.canBeConsideredAnAlly(wolf, other);
        }
        return false;
    }

    private static void onHowlStarted(Wolf wolf){
        GenericAi.stopWalking(wolf);
        wolf.playSound(SoundEvents.WOLF_HOWL, HOWL_VOLUME, wolf.getVoicePitch());
        wolf.gameEvent(GameEvent.ENTITY_ROAR);
    }

    private static float getListenerSpeedModifier(LivingEntity livingEntity){
        return SharedWolfAi.SPEED_MODIFIER_WALKING;
    }

    public static boolean isInteresting(Wolf wolf, ItemStack stack) {
        return wolf.isFood(stack) || stack.is(COTWTags.WOLF_LOVED);
    }

    private static boolean canHunt(Wolf wolf){
        return !wolf.isBaby()
                && !AiUtil.hasAnyMemory(wolf, MemoryModuleType.ANGRY_AT, COTWMemoryModuleTypes.STALK_TARGET.get())
                && !HunterAi.hasAnyoneNearbyHuntedRecently(wolf, GenericAi.getNearbyAdults(wolf))
                && SharedWolfAi.canStartAttacking(wolf);
    }
}
