package com.infamous.all_bark_all_bite.common.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.all_bark_all_bite.common.ai.*;
import com.infamous.all_bark_all_bite.common.behavior.*;
import com.infamous.all_bark_all_bite.common.behavior.hunter.*;
import com.infamous.all_bark_all_bite.common.behavior.pet.OwnerHurtByTarget;
import com.infamous.all_bark_all_bite.common.behavior.pet.OwnerHurtTarget;
import com.infamous.all_bark_all_bite.common.behavior.pet.SitWhenOrderedTo;
import com.infamous.all_bark_all_bite.common.behavior.sleep.SleepOnGround;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import com.infamous.all_bark_all_bite.common.util.BrainUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class SharedWolfBrain {

    public static AnimalMakeLove createBreedBehavior(EntityType<? extends TamableAnimal> type) {
        return new AnimalMakeLove(type, SharedWolfAi.SPEED_MODIFIER_BREEDING);
    }

    public static <E extends TamableAnimal> ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> getFightPackage(BiPredicate<E, LivingEntity> huntTargetPredicate) {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove),
                        new LeapAtTarget(SharedWolfAi.LEAP_YD, SharedWolfAi.TOO_CLOSE_TO_LEAP, SharedWolfAi.TOO_FAR_TO_LEAP, SharedWolfAi.LEAP_COOLDOWN),
                        new RunIf<>(Entity::isOnGround, new SetWalkTargetFromAttackTargetIfTargetOutOfReach(SharedWolfAi.SPEED_MODIFIER_CHASING), true),
                        new MeleeAttack(SharedWolfAi.ATTACK_COOLDOWN_TICKS),
                        new RememberIfHuntTargetWasKilled<>(huntTargetPredicate, SharedWolfAi.TIME_BETWEEN_HUNTS),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)));
    }

    public static <E extends TamableAnimal> ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> getTargetPackage(BiConsumer<E, LivingEntity> onHurtByEntity, Predicate<E> canStartHunting, Predicate<E> canStartStalking){
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new OwnerHurtByTarget<>(SharedWolfAi::canDefendOwner, SharedWolfAi::wantsToAttack),
                        new OwnerHurtTarget<>(SharedWolfAi::canDefendOwner, SharedWolfAi::wantsToAttack),
                        new HurtByEntityTrigger<>(onHurtByEntity),
                        new StartAttacking<>(SharedWolfAi::canStartAttacking, SharedWolfAi::findNearestValidAttackTarget),
                        new StartStalkingPrey<>(canStartStalking, SharedWolfBrain::findNearestValidStalkTarget),
                        new StartHuntingPrey<>(canStartHunting, SharedWolfAi.TIME_BETWEEN_HUNTS),
                        new StopAttackingIfTargetInvalid<>(),
                        new StopBeingAngryIfTargetDead<>()
                ));
    }

    public static void onHurtBy(TamableAnimal tamableAnimal){
        SharedWolfAi.stopHoldingItemInMouth(tamableAnimal);
        SharedWolfAi.clearStates(tamableAnimal, true);
        tamableAnimal.setOrderedToSit(false);
    }

    public static boolean canStartStalking(TamableAnimal wolf){
        if(wolf.isBaby()){
            return SharedWolfAi.canMove(wolf);
        } else{
            return GenericAi.getNearbyVisibleAdults(wolf).isEmpty() && SharedWolfAi.canStartAttacking(wolf) && !HunterAi.hasHuntedRecently(wolf);
        }
    }

    private static Optional<? extends LivingEntity> findNearestValidStalkTarget(TamableAnimal wolf){
        if(wolf.isBaby()){
            return GenericAi.getNearestVisibleBabies(wolf).stream().filter(target -> canStartStalking(wolf, target)).findFirst();
        } else{
            return HunterAi.getNearestVisibleHuntable(wolf).filter(target -> canStartStalking(wolf, target));
        }
    }

    private static boolean canStartStalking(TamableAnimal wolf, LivingEntity target) {
        return wolf.distanceToSqr(target) > getPounceDistance(wolf) && !AiUtil.isLookingAtMe(wolf, target, StalkPrey.INITIAL_VISION_OFFSET);
    }

    public static boolean canStartHunting(TamableAnimal wolf){
        return HunterAi.getStalkTarget(wolf).isEmpty() && SharedWolfAi.canStartAttacking(wolf);
    }

    public static <E extends TamableAnimal> ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> getUpdatePackage(List<Activity> activities, BiConsumer<E, Pair<Activity, Activity>> onActivityChanged){
        return BrainUtil.createPriorityPairs(99,
                ImmutableList.of(
                        new UpdateActivity<>(activities, onActivityChanged),
                        new UpdateTarget()
                ));
    }

    public static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super TamableAnimal>>> getPanicPackage(){
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove)
                ));
    }

    public static <E extends TamableAnimal> ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> getSitPackage(RunOne<TamableAnimal> idleLookBehaviors, Behavior<E> beg){
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new SitWhenOrderedTo(),
                        beg,
                        idleLookBehaviors
                ));
    }

    public static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super TamableAnimal>>> getPouncePackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Pounce(SharedWolfBrain::getPounceDistance, SharedWolfBrain::getPounceHeight, SharedWolfBrain::getPounceCooldown),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, ABABMemoryModuleTypes.POUNCE_TARGET.get())
                ));
    }

    private static int getPounceCooldown(PathfinderMob mob){
        return mob.isBaby() ? SharedWolfAi.PLAY_POUNCE_COOLDOWN.sample(mob.getRandom()) : SharedWolfAi.HUNT_POUNCE_COOLDOWN.sample(mob.getRandom());
    }

    public static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super TamableAnimal>>> getStalkPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new StalkPrey(SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfBrain::getPounceDistance, SharedWolfBrain::getPounceHeight),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, ABABMemoryModuleTypes.STALK_TARGET.get())
                ));
    }

    private static int getPounceDistance(PathfinderMob mob){
        return mob.isBaby() ? SharedWolfAi.BABY_POUNCE_DISTANCE : SharedWolfAi.POUNCE_DISTANCE;
    }

    private static int getPounceHeight(PathfinderMob mob){
        return mob.isBaby() ? SharedWolfAi.BABY_POUNCE_HEIGHT : SharedWolfAi.POUNCE_HEIGHT;
    }

    public static <E extends TamableAnimal> ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> getAvoidPackage(Predicate<E> stopAvoidingIf, RunOne<TamableAnimal> idleMovementBehaviors, RunOne<TamableAnimal> idleLookBehaviors) {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove),
                        SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, SharedWolfAi.SPEED_MODIFIER_RETREATING, SharedWolfAi.DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING, true),
                        idleMovementBehaviors,
                        idleLookBehaviors,
                        new EraseMemoryIf<>(stopAvoidingIf, MemoryModuleType.AVOID_TARGET)));
    }

    public static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super TamableAnimal>>> getMeetPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove),
                        new StayCloseToTarget<>(SharedWolfAi::getHowlPosition, SharedWolfAi.ADULT_FOLLOW_RANGE.getMinValue() - 1, SharedWolfAi.ADULT_FOLLOW_RANGE.getMaxValue(), SharedWolfAi.SPEED_MODIFIER_WALKING),
                        new EraseMemoryIf<>(SharedWolfBrain::wantsToStopFollowingHowl, ABABMemoryModuleTypes.HOWL_LOCATION.get()))
        );
    }

    private static boolean wantsToStopFollowingHowl(TamableAnimal wolf){
        Optional<PositionTracker> howlPosition = SharedWolfAi.getHowlPosition(wolf);
        if (howlPosition.isEmpty()) {
            return true;
        } else {
            PositionTracker tracker = howlPosition.get();
            return wolf.position().closerThan(tracker.currentPosition(), SharedWolfAi.ADULT_FOLLOW_RANGE.getMaxValue());
        }
    }

    public static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super TamableAnimal>>> getRestPackage(RunOne<TamableAnimal> idleLookBehaviors, boolean nocturnal){
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new SleepOnGround<>(wolf -> SharedWolfAi.canSleep(wolf, nocturnal), SharedWolfAi::handleSleeping),
                        new RunIf<>(Predicate.not(LivingEntity::isSleeping), idleLookBehaviors, true)
                ));
    }

    public static Set<Pair<MemoryModuleType<?>, MemoryStatus>> getPanicConditions() {
        return ImmutableSet.of(Pair.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_PRESENT));
    }

    public static Set<Pair<MemoryModuleType<?>, MemoryStatus>> getSitConditions() {
        return ImmutableSet.of(Pair.of(ABABMemoryModuleTypes.IS_ORDERED_TO_SIT.get(), MemoryStatus.VALUE_PRESENT));
    }

    public static Set<Pair<MemoryModuleType<?>, MemoryStatus>> getRestConditions(MemoryModuleType<Unit> timeMemory) {
        return Util.make(() -> {
            Set<Pair<MemoryModuleType<?>, MemoryStatus>> restConditions = new HashSet<>();
            restConditions.add(Pair.of(ABABMemoryModuleTypes.IS_SHELTERED.get(), MemoryStatus.VALUE_PRESENT));
            restConditions.add(Pair.of(timeMemory, MemoryStatus.VALUE_PRESENT));
            restConditions.add(Pair.of(ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get(), MemoryStatus.VALUE_ABSENT));
            restConditions.add(Pair.of(ABABMemoryModuleTypes.IS_ORDERED_TO_HEEL.get(), MemoryStatus.VALUE_ABSENT));
            restConditions.add(Pair.of(ABABMemoryModuleTypes.IS_ALERT.get(), MemoryStatus.VALUE_ABSENT));
            restConditions.add(Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
            restConditions.add(Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT));
            return restConditions;
        });
    }

    public static boolean isFollowingOwner(TamableAnimal dog) {
        return CommandAi.isFollowing(dog) || CommandAi.isHeeling(dog);
    }

    public static void fetchItem(LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        brain.eraseMemory(ABABMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get());
        brain.setMemory(ABABMemoryModuleTypes.FETCHING_ITEM.get(), true);
    }

    public static void onAgeChanged(TamableAnimal wolf){
        HunterAi.clearPounceCooldown(wolf);
    }

    public static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super TamableAnimal>>> getCountDownPackage(){
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(ABABMemoryModuleTypes.POUNCE_COOLDOWN_TICKS.get()),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)
                ));
    }
}
