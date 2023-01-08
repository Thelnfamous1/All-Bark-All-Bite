package com.infamous.call_of_the_wild.common.entity.wolf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.behavior.HurtByTrigger;
import com.infamous.call_of_the_wild.common.behavior.LeapAtTarget;
import com.infamous.call_of_the_wild.common.behavior.MoveToNonSkySeeingSpot;
import com.infamous.call_of_the_wild.common.behavior.PerchAndSearch;
import com.infamous.call_of_the_wild.common.behavior.hunter.RememberIfHuntTargetWasKilled;
import com.infamous.call_of_the_wild.common.behavior.hunter.StalkPrey;
import com.infamous.call_of_the_wild.common.behavior.hunter.StartHunting;
import com.infamous.call_of_the_wild.common.behavior.long_jump.LongJumpToTarget;
import com.infamous.call_of_the_wild.common.behavior.pack.FollowPackLeader;
import com.infamous.call_of_the_wild.common.behavior.pack.StartHowling;
import com.infamous.call_of_the_wild.common.behavior.pack.ValidateFollowers;
import com.infamous.call_of_the_wild.common.behavior.pack.ValidateLeader;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtByTarget;
import com.infamous.call_of_the_wild.common.behavior.pet.StopSittingToWalk;
import com.infamous.call_of_the_wild.common.behavior.sleep.StartSleeping;
import com.infamous.call_of_the_wild.common.behavior.sleep.WakeUpTrigger;
import com.infamous.call_of_the_wild.common.entity.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WolfGoalPackages {
    public static final float MAX_JUMP_VELOCITY = 1.5F;
    static final UniformInt TIME_BETWEEN_LONG_JUMPS = TimeUtil.rangeOfSeconds(5, 7);

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getCorePackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new HurtByTrigger<>(WolfGoalPackages::wasHurtBy),
                        new WakeUpTrigger<>(WolfGoalPackages::wantsToWakeUp),
                        new ValidateLeader(),
                        new ValidateFollowers(),
                        new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS),

                        new Swim(SharedWolfAi.JUMP_CHANCE_IN_WATER),
                        new RunIf<>(SharedWolfAi::shouldPanic, new AnimalPanic(SharedWolfAi.SPEED_MODIFIER_PANICKING), true),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),

                        new StopSittingToWalk(),
                        new OwnerHurtByTarget(),
                        new CopyMemoryWithExpiry<>(
                                SharedWolfAi::isNearDisliked,
                                ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                                MemoryModuleType.AVOID_TARGET,
                                SharedWolfAi.AVOID_DURATION),
                        new StopBeingAngryIfTargetDead<>()));
    }

    private static boolean wantsToWakeUp(Wolf wolf){
        Optional<BlockPos> sleepingPos = wolf.getSleepingPos();
        if (sleepingPos.isEmpty()) {
            return true;
        } else {
            return !wolf.getBrain().isActive(Activity.REST)
                    || !sleepingPos.get().closerToCenterThan(wolf.position(), 1.14D)
                    || !SharedWolfAi.canSleep(wolf);
        }
    }

    private static void wasHurtBy(Wolf wolf, LivingEntity attacker) {
        wolf.setIsInterested(false);
        SharedWolfAi.clearStates(wolf);

        AiUtil.eraseAllMemories(wolf,
                MemoryModuleType.BREED_TARGET,
                ABABMemoryModuleTypes.HOWL_LOCATION.get());

        SharedWolfAi.reactToAttack(wolf, attacker);
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getFightPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new StopAttackingIfTargetInvalid<>(WolfGoalPackages::onTargetErased),
                        new StalkPrey<>(WolfGoalPackages::wantsToStalk,
                                SharedWolfAi.SPEED_MODIFIER_WALKING,
                                SharedWolfAi.POUNCE_DISTANCE,
                                WolfGoalPackages::isPreparingToPounce,
                                WolfGoalPackages::toggleIsPreparingToPounce,
                                MAX_JUMP_VELOCITY),
                        BrainUtil.gateBehaviors(
                                ImmutableMap.of(
                                        ABABMemoryModuleTypes.IS_STALKING.get(), MemoryStatus.VALUE_ABSENT,
                                        ABABMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_ABSENT
                                ),
                                ImmutableSet.of(),
                                GateBehavior.OrderPolicy.ORDERED,
                                GateBehavior.RunningPolicy.TRY_ALL,
                                ImmutableList.of(
                                        Pair.of(new SetWalkTargetFromAttackTargetIfTargetOutOfReach(SharedWolfAi.SPEED_MODIFIER_CHASING), 1),
                                        Pair.of(new LeapAtTarget(SharedWolfAi.LEAP_YD, SharedWolfAi.TOO_CLOSE_TO_LEAP, SharedWolfAi.POUNCE_DISTANCE), 1),
                                        Pair.of(new MeleeAttack(SharedWolfAi.ATTACK_COOLDOWN_TICKS), 1)
                                )
                        ),
                        new RememberIfHuntTargetWasKilled<>(WolfGoalPackages::isHuntTarget, SharedWolfAi.TIME_BETWEEN_HUNTS),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)));
    }

    private static void onTargetErased(Wolf wolf, LivingEntity target){
        wolf.setTarget(null);
    }

    public static boolean wantsToStalk(Wolf wolf, LivingEntity target){
        return GenericAi.getNearbyVisibleAdults(wolf).isEmpty() && target.getType().is(ABABTags.WOLF_HUNT_TARGETS);
    }

    private static boolean isPreparingToPounce(Wolf wolf){
        return wolf.hasPose(Pose.CROUCHING) && wolf.isInterested();
    }

    private static void toggleIsPreparingToPounce(Wolf wolf, boolean flag){
        wolf.setIsInterested(flag);
        if(flag){
            wolf.setPose(Pose.CROUCHING);
        } else{
            if(wolf.hasPose(Pose.CROUCHING)) wolf.setPose(Pose.STANDING);
        }
    }

    private static boolean isHuntTarget(Wolf wolf, LivingEntity target) {
        return AiUtil.isHuntTarget(wolf, target, ABABTags.WOLF_HUNT_TARGETS);
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
                        Pair.of(new SetWalkTargetFromLookTarget(SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_LOOK_TARGET), 2),
                        Pair.of(new RunIf<>(WolfGoalPackages::isNotAlert, new PerchAndSearch<>(Wolf::isInSittingPose, Wolf::setInSittingPose), true), 2),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    private static boolean isNotAlert(Wolf wolf){
        return !SharedWolfAi.alertable(wolf);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static boolean wantsToStopFleeing(Wolf wolf) {
        Brain<?> brain = wolf.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        } else {
            LivingEntity avoidTarget = brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
            if (!WolfAi.isDisliked(wolf, avoidTarget)) {
                return !brain.isMemoryValue(ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), avoidTarget);
            } else {
                return false;
            }
        }
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getLongJumpPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new LongJumpMidJump(TIME_BETWEEN_LONG_JUMPS, SoundEvents.WOLF_STEP),
                        new LongJumpToTarget<>(TIME_BETWEEN_LONG_JUMPS, MAX_JUMP_VELOCITY,
                                wolf -> SoundEvents.WOLF_STEP, 0),
                        new EraseMemoryIf<>(LongJumpAi::isOnJumpCooldown, ABABMemoryModuleTypes.LONG_JUMP_TARGET.get())
                )
        );
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getMeetPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new StayCloseToTarget<>(SharedWolfAi::getHowlPosition, SharedWolfAi.ADULT_FOLLOW_RANGE.getMinValue() - 1, SharedWolfAi.ADULT_FOLLOW_RANGE.getMaxValue(), SharedWolfAi.SPEED_MODIFIER_WALKING),
                        new EraseMemoryIf<>(WolfGoalPackages::wantsToStopFollowingHowl, ABABMemoryModuleTypes.HOWL_LOCATION.get()))
        );
    }

    private static boolean wantsToStopFollowingHowl(Wolf wolf){
        Optional<PositionTracker> howlPosition = SharedWolfAi.getHowlPosition(wolf);
        if (howlPosition.isEmpty()) {
            return true;
        } else {
            PositionTracker tracker = howlPosition.get();
            return wolf.position().closerThan(tracker.currentPosition(), SharedWolfAi.ADULT_FOLLOW_RANGE.getMaxValue());
        }
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getRestPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new EraseMemoryIf<>(WolfGoalPackages::isNotSleeping, ABABMemoryModuleTypes.IS_SLEEPING.get())
                ));
    }

    private static boolean isNotSleeping(Wolf wolf){
        return !wolf.isSleeping();
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getIdlePackage() {
        return BrainUtil.createPriorityPairs(0, ImmutableList.of(

                new AnimalMakeLove(EntityType.WOLF, SharedWolfAi.SPEED_MODIFIER_BREEDING),
                new StartAttacking<>(SharedWolfAi::canStartAttacking, SharedWolfAi::findNearestValidAttackTarget),
                new StartHunting<>(WolfGoalPackages::canHunt, SharedWolfAi::startHunting, SharedWolfAi.TIME_BETWEEN_HUNTS),

                // if not breeding or attacking, then try to sleep
                BrainUtil.gateBehaviors(
                        ImmutableMap.of(
                                MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT,
                                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                                MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_ABSENT
                        ),
                        ImmutableSet.of(),
                        GateBehavior.OrderPolicy.ORDERED,
                        GateBehavior.RunningPolicy.TRY_ALL,
                        ImmutableList.of(
                                Pair.of(new RunIf<>(WolfGoalPackages::wantsToFindShelter, new MoveToNonSkySeeingSpot(SharedWolfAi.SPEED_MODIFIER_WALKING)), 1),
                                Pair.of(new RunIf<>(SharedWolfAi::canSleep, new StartSleeping()), 1),
                                // if not seeking shelter or sleeping, then try to socialize
                                Pair.of(BrainUtil.gateBehaviors(
                                        ImmutableMap.of(
                                                ABABMemoryModuleTypes.IS_SLEEPING.get(), MemoryStatus.VALUE_ABSENT,
                                                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
                                        ),
                                        ImmutableSet.of(),
                                        GateBehavior.OrderPolicy.ORDERED,
                                        GateBehavior.RunningPolicy.TRY_ALL,
                                        ImmutableList.of(
                                                Pair.of(new FollowPackLeader<>(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT), 1),
                                                Pair.of(new BabyFollowAdult<>(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT), 1),
                                                Pair.of(new StartHowling<>(SharedWolfAi.TIME_BETWEEN_HOWLS, SharedWolfAi.ADULT_FOLLOW_RANGE.getMaxValue()), 1),
                                                // if not socializing, then be idle
                                                Pair.of(BrainUtil.gateBehaviors(
                                                        ImmutableMap.of(
                                                                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
                                                        ),
                                                        ImmutableSet.of(),
                                                        GateBehavior.OrderPolicy.ORDERED,
                                                        GateBehavior.RunningPolicy.TRY_ALL,
                                                        ImmutableList.of(
                                                                Pair.of(createIdleLookBehaviors(), 1),
                                                                Pair.of(createIdleMovementBehaviors(), 1)
                                                        )
                                                ), 1)
                                        )
                                ), 1)
                        ))
                ));
    }

    private static boolean wantsToFindShelter(LivingEntity livingEntity){
        return livingEntity.level.isThundering() || livingEntity.level.isDay();
    }

    private static boolean canHunt(Wolf wolf){
        return !wolf.isBaby()
                && !AngerAi.hasAngryAt(wolf)
                && !HunterAi.hasAnyoneNearbyHuntedRecently(wolf, GenericAi.getNearbyAdults(wolf))
                && SharedWolfAi.canStartAttacking(wolf);
    }
}
