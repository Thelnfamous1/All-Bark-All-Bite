package com.infamous.all_bark_all_bite.common.entity.wolf;

import com.google.common.collect.ImmutableList;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.ai.TrustAi;
import com.infamous.all_bark_all_bite.common.behavior.*;
import com.infamous.all_bark_all_bite.common.behavior.pack.HowlForPack;
import com.infamous.all_bark_all_bite.common.behavior.pack.JoinOrCreatePackAndFollow;
import com.infamous.all_bark_all_bite.common.behavior.pack.ValidateFollowers;
import com.infamous.all_bark_all_bite.common.behavior.pack.ValidateLeader;
import com.infamous.all_bark_all_bite.common.behavior.sleep.WakeUpTrigger;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfBrain;
import com.infamous.all_bark_all_bite.common.logic.BrainMaker;
import com.infamous.all_bark_all_bite.common.registry.ABABActivities;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import com.infamous.all_bark_all_bite.common.util.BrainUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.function.Predicate;

public class WolfBrain {

    public static Brain<Wolf> makeBrain(Brain<Wolf> brain) {
        BrainMaker<Wolf> brainMaker = new BrainMaker<>(brain);
        brainMaker.initActivityWithConditions(Activity.PANIC,
                SharedWolfBrain.getPanicPackage(), SharedWolfBrain.getPanicConditions());
        brainMaker.initActivityWithConditions(ABABActivities.SIT.get(),
                SharedWolfBrain.getSitPackage(createIdleLookBehaviors(), beg()), SharedWolfBrain.getSitConditions());
        brainMaker.initActivityWithMemoryGate(ABABActivities.POUNCE.get(),
                SharedWolfBrain.getPouncePackage(), ABABMemoryModuleTypes.POUNCE_TARGET.get());
        brainMaker.initActivityWithMemoryGate(Activity.FIGHT,
                SharedWolfBrain.getFightPackage(WolfBrain::isHuntTarget), MemoryModuleType.ATTACK_TARGET);
        brainMaker.initActivityWithMemoryGate(Activity.AVOID,
                SharedWolfBrain.getAvoidPackage(WolfBrain::wantsToStopFleeing, createIdleMovementBehaviors(), createIdleLookBehaviors()), MemoryModuleType.AVOID_TARGET);
        brainMaker.initActivityWithMemoryGate(Activity.MEET,
                SharedWolfBrain.getMeetPackage(), ABABMemoryModuleTypes.HOWL_LOCATION.get());
        brainMaker.initActivityWithMemoryGate(ABABActivities.STALK.get(),
                SharedWolfBrain.getStalkPackage(), ABABMemoryModuleTypes.STALK_TARGET.get());
        brainMaker.initActivityWithConditions(Activity.REST,
                SharedWolfBrain.getRestPackage(createIdleLookBehaviors(), true), SharedWolfBrain.getRestConditions(ABABMemoryModuleTypes.IS_LEVEL_DAY.get()));
        brainMaker.initActivity(Activity.IDLE,
                getIdlePackage());

        brainMaker.initCoreActivity(Activity.CORE,
                getCorePackage());
        brainMaker.initCoreActivity(ABABActivities.COUNT_DOWN.get(),
                SharedWolfBrain.getCountDownPackage());
        brainMaker.initCoreActivity(ABABActivities.TARGET.get(),
                SharedWolfBrain.getTargetPackage(WolfBrain::wasHurtBy, SharedWolfBrain::canStartHunting, SharedWolfBrain::canStartStalking));
        brainMaker.initCoreActivity(ABABActivities.UPDATE.get(),
                getUpdatePackage(brainMaker.getActivities()));

        return brainMaker.makeBrain(Activity.IDLE);
    }

    private static RunIf<Wolf> beg() {
        return new RunIf<>(TamableAnimal::isTame, new Beg<>(Wolf::isFood, Wolf::setIsInterested, SharedWolfAi.MAX_LOOK_DIST), true);
    }

    private static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getCorePackage(){
        return BrainUtil.createPriorityPairs(0, ImmutableList.of(
                new HurtByTrigger<>(SharedWolfBrain::onHurtBy),
                new WakeUpTrigger<>(SharedWolfAi::wantsToWakeUp),
                new AgeChangeTrigger<>(SharedWolfBrain::onAgeChanged),
                new Swim(SharedWolfAi.JUMP_CHANCE_IN_WATER),
                new RunIf<>(SharedWolfAi::shouldPanic, new AnimalPanic(SharedWolfAi.SPEED_MODIFIER_PANICKING), true),
                new Eat(SharedWolfAi::setAteRecently, SharedWolfAi.EAT_DURATION),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CopyMemoryWithExpiry<>(
                        SharedWolfAi::isNearDisliked,
                        ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                        MemoryModuleType.AVOID_TARGET,
                        SharedWolfAi.AVOID_DURATION),
                new UpdateUnitMemory<>(TamableAnimal::isOrderedToSit, ABABMemoryModuleTypes.IS_ORDERED_TO_SIT.get()),
                new UpdateUnitMemory<>(SharedWolfAi::hasShelter, ABABMemoryModuleTypes.IS_SHELTERED.get()),
                new UpdateUnitMemory<>(SharedWolfAi::isInDayTime, ABABMemoryModuleTypes.IS_LEVEL_DAY.get()),
                new UpdateUnitMemory<>(WolfBrain::isAlert, ABABMemoryModuleTypes.IS_ALERT.get()),
                new UpdateUnitMemory<>(LivingEntity::isSleeping, ABABMemoryModuleTypes.IS_SLEEPING.get()),
                new ValidateLeader(),
                new ValidateFollowers()
        ));
    }

    private static boolean isAlert(Wolf wolf) {
        return SharedWolfAi.alertable(wolf, ABABTags.WOLF_HUNT_TARGETS, ABABTags.WOLF_ALWAYS_HOSTILES);
    }

    private static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getUpdatePackage(List<Activity> activities){
        return BrainUtil.createPriorityPairs(99,
                ImmutableList.of(
                        new UpdateActivity<>(activities, WolfBrain::onActivityChanged),
                        new UpdateTarget(),
                        new UpdateNeutralMob<>()
                ));
    }

    private static void wasHurtBy(Wolf wolf, LivingEntity attacker) {
        AiUtil.eraseMemories(wolf,
                MemoryModuleType.BREED_TARGET,
                ABABMemoryModuleTypes.STALK_TARGET.get(),
                ABABMemoryModuleTypes.POUNCE_TARGET.get(),
                ABABMemoryModuleTypes.HOWL_LOCATION.get());

        if(TrustAi.likes(wolf, attacker)){
            TrustAi.decrementTrust(wolf, WolfAi.TRUST_INCREMENT);
        }

        SharedWolfAi.reactToAttack(wolf, attacker);
    }

    private static void onActivityChanged(Wolf wolf, Pair<Activity, Activity> activityChange){
        WolfAi.getSoundForCurrentActivity(wolf).ifPresent(se -> AiUtil.playSoundEvent(wolf, se));
    }

    private static boolean isHuntTarget(Wolf wolf, LivingEntity target) {
        return target.getType().is(ABABTags.WOLF_HUNT_TARGETS);
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

    private static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Wolf>>> getIdlePackage(){
        return BrainUtil.createPriorityPairs(0, ImmutableList.of(BrainUtil.tryAllBehaviorsInOrderIfAbsent(
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove, SharedWolfAi.TOO_FAR_FROM_WALK_TARGET),
                        new RunIf<>(SharedWolfBrain::isFollowingOwner, SharedWolfAi.createFollowOwner(SharedWolfAi.SPEED_MODIFIER_WALKING), true),
                        BrainUtil.tryAllBehaviorsInOrderIfAbsent(
                                ImmutableList.of(
                                        new RunIf<>(WolfBrain::canBeTempted, new FollowTemptation(SharedWolfAi::getSpeedModifierTempted), true),
                                        SharedWolfBrain.createBreedBehavior(EntityType.WOLF),
                                        new RunIf<>(livingEntity -> SharedWolfAi.wantsToFindShelter(livingEntity, true), new MoveToNonSkySeeingSpot(SharedWolfAi.SPEED_MODIFIER_WALKING), true),
                                        new HowlForPack<>(Predicate.not(TamableAnimal::isTame), SharedWolfAi.TIME_BETWEEN_HOWLS, SharedWolfAi.ADULT_FOLLOW_RANGE.getMaxValue()),
                                        new JoinOrCreatePackAndFollow<>(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT),
                                        new BabyFollowAdult<>(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT)
                                ),
                                ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get(),
                                ABABMemoryModuleTypes.IS_ORDERED_TO_HEEL.get()
                        ),
                        SharedWolfAi.createGoToWantedItem(false),
                        createIdleMovementBehaviors(),
                        beg(),
                        createIdleLookBehaviors()
                ), ABABMemoryModuleTypes.IS_SLEEPING.get(), ABABMemoryModuleTypes.IS_ORDERED_TO_SIT.get())));
    }

    private static boolean canBeTempted(Wolf wolf){
        return wolf.isTame() || GenericAi.getTemptingPlayer(wolf).map(temptingPlayer -> WolfAi.isOwnedByOrLikes(wolf, temptingPlayer)).orElse(false);
    }

    private static RunOne<TamableAnimal> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(EntityType.WOLF, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<TamableAnimal> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(SharedWolfAi.SPEED_MODIFIER_WALKING), 3),
                        Pair.of(InteractWith.of(EntityType.WOLF, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(new SetWalkTargetFromLookTarget(SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_LOOK_TARGET), 2),
                        Pair.of(new PerchAndSearch<>(SharedWolfBrain::canPerch, TamableAnimal::setInSittingPose), 2),
                        Pair.of(new DoNothing(30, 60), 1)));
    }
}
