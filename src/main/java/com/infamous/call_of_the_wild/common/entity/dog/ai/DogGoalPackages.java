package com.infamous.call_of_the_wild.common.entity.dog.ai;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.behavior.Beg;
import com.infamous.call_of_the_wild.common.behavior.HurtByTrigger;
import com.infamous.call_of_the_wild.common.behavior.LeapAtTarget;
import com.infamous.call_of_the_wild.common.behavior.dig.DigAtLocation;
import com.infamous.call_of_the_wild.common.behavior.hunter.RememberIfHuntTargetWasKilled;
import com.infamous.call_of_the_wild.common.behavior.hunter.StartHunting;
import com.infamous.call_of_the_wild.common.behavior.item.*;
import com.infamous.call_of_the_wild.common.behavior.pet.FollowOwner;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtByTarget;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtTarget;
import com.infamous.call_of_the_wild.common.behavior.pet.SitWhenOrderedTo;
import com.infamous.call_of_the_wild.common.registry.COTWEntityTypes;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.*;
import com.infamous.call_of_the_wild.data.COTWBuiltInLootTables;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DogGoalPackages {
    public static final int ITEM_PICKUP_COOLDOWN = 60;
    public static final int MAX_FETCH_DISTANCE = 16;
    public static final int DISABLE_FETCH_TIME = 200;
    public static final int MAX_TIME_TO_REACH_ITEM = 200;
    static final float SPEED_MODIFIER_FETCHING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    private static final long DIG_DURATION = 100L;

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getCorePackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Swim(SharedWolfAi.JUMP_CHANCE_IN_WATER),
                        new RunIf<>(SharedWolfAi::shouldPanic, new AnimalPanic(SharedWolfAi.SPEED_MODIFIER_PANICKING), true),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new SitWhenOrderedTo(),
                        new OwnerHurtByTarget(),
                        new OwnerHurtTarget(),
                        new CopyMemoryWithExpiry<>(
                                SharedWolfAi::isNearDisliked,
                                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                                MemoryModuleType.AVOID_TARGET,
                                SharedWolfAi.AVOID_DURATION),
                        new StopHoldingItemIfNoLongerInItemActivity<>(DogGoalPackages::canStopHolding, DogGoalPackages::stopHoldingItemInMouth, COTWMemoryModuleTypes.FETCHING_ITEM.get()),
                        new RunIf<>(DogGoalPackages::canFetch, new StartItemActivityWithItemIfSeen<>(DogGoalPackages::canFetch, COTWMemoryModuleTypes.FETCHING_ITEM.get(), COTWMemoryModuleTypes.FETCHING_DISABLED.get(), COTWMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get())),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS),
                        new HurtByTrigger<>(DogGoalPackages::wasHurtBy),
                        new StopBeingAngryIfTargetDead<>()));
    }

    private static boolean canStopHolding(Dog dog) {
        return dog.hasItemInMouth() && !AiUtil.hasAnyMemory(dog, COTWMemoryModuleTypes.DIG_LOCATION.get());
    }

    static void stopHoldingItemInMouth(Dog dog) {
        ItemStack mouthStack = dog.getItemInMouth();
        dog.setItemInMouth(ItemStack.EMPTY);
        BehaviorUtils.throwItem(dog, mouthStack, GenericAi.getRandomNearbyPos(dog, 4, 2));
        onThrown(dog);
    }

    private static boolean canFetch(Dog dog, ItemEntity itemEntity){
        return canFetch(itemEntity.getItem()) && itemEntity.closerThan(dog, MAX_FETCH_DISTANCE);
    }

    static boolean canFetch(Dog dog){
        return SharedWolfAi.canMove(dog) && dog.isTame();
    }

    protected static boolean canFetch(ItemStack stack) {
        return stack.is(COTWTags.DOG_FETCHES);
    }

    private static void wasHurtBy(Dog dog, LivingEntity attacker) {
        if (dog.hasItemInMouth()) {
            stopHoldingItemInMouth(dog);
        }

        dog.setIsInterested(false);
        SharedWolfAi.clearStates(dog);

        AiUtil.eraseAllMemories(dog,
                MemoryModuleType.BREED_TARGET,
                COTWMemoryModuleTypes.FETCHING_ITEM.get(),
                COTWMemoryModuleTypes.DIG_LOCATION.get());

        SharedWolfAi.tellAlliesIWasAttacked(dog, attacker);
    }

    static void onThrown(Dog dog){
        dog.playSoundEvent(SoundEvents.FOX_SPIT);
        setItemPickupCooldown(dog);
    }

    static void setItemPickupCooldown(Dog dog) {
        dog.getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, ITEM_PICKUP_COOLDOWN);
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getFightPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new StopAttackingIfTargetInvalid<>(),
                        new RunIf<>(SharedWolfAi::canStartAttacking, new SetWalkTargetFromAttackTargetIfTargetOutOfReach(SharedWolfAi.SPEED_MODIFIER_CHASING)),
                        new RunIf<>(SharedWolfAi::canStartAttacking, new LeapAtTarget(SharedWolfAi.LEAP_YD, SharedWolfAi.TOO_CLOSE_TO_LEAP, SharedWolfAi.POUNCE_DISTANCE), true),
                        new RunIf<>(SharedWolfAi::canStartAttacking, new MeleeAttack(SharedWolfAi.ATTACK_COOLDOWN_TICKS)),
                        new RememberIfHuntTargetWasKilled<>(DogGoalPackages::isHuntTarget, SharedWolfAi.TIME_BETWEEN_HUNTS),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)));
    }

    private static boolean isHuntTarget(Dog dog, LivingEntity target) {
        return AiUtil.isHuntTarget(dog, target, COTWTags.DOG_HUNT_TARGETS);
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getAvoidPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new RunIf<>(SharedWolfAi::canAvoid, SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, SharedWolfAi.SPEED_MODIFIER_RETREATING, SharedWolfAi.DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING, true)),
                        createIdleLookBehaviors(),
                        new RunIf<>(SharedWolfAi::canWander, createIdleMovementBehaviors(), true),
                        new EraseMemoryIf<>(DogGoalPackages::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)));
    }

    static RunOne<Dog> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(COTWEntityTypes.DOG.get(), SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    static RunOne<Dog> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(SharedWolfAi.SPEED_MODIFIER_WALKING), 2),
                        Pair.of(InteractWith.of(COTWEntityTypes.DOG.get(), SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(InteractWith.of(EntityType.PLAYER, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(InteractWith.of(EntityType.VILLAGER, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(new RunIf<>(GenericAi::doesntSeeAnyPlayerHoldingWantedItem, new SetWalkTargetFromLookTarget(SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_LOOK_TARGET)), 2),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static boolean wantsToStopFleeing(TamableAnimal tamableAnimal) {
        if(tamableAnimal.isTame()) return true;

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
        return entityType.is(COTWTags.DOG_DISLIKED);
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getDigPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new RunIf<>(DogGoalPackages::canFetch, new StayCloseToTarget<>(DogGoalPackages::getDigPosition, 1, 2, SPEED_MODIFIER_FETCHING)),
                        new RunIf<>(DogGoalPackages::canFetch, new DigAtLocation<>(DogGoalPackages::onDigCompleted, DIG_DURATION), true)));
    }

    private static Optional<PositionTracker> getDigPosition(LivingEntity dog) {
        Brain<?> brain = dog.getBrain();
        Optional<GlobalPos> digLocation = DigAi.getDigLocation(dog);
        if (digLocation.isPresent()) {
            GlobalPos digPos = digLocation.get();
            Level level = dog.getLevel();
            if (level.dimension() == digPos.dimension() && level.getBlockState(digPos.pos()).is(COTWTags.DOG_CAN_DIG)) {
                return Optional.of(new BlockPosTracker(digPos.pos()));
            }

            brain.eraseMemory(COTWMemoryModuleTypes.DIG_LOCATION.get());
        }

        return Optional.empty();
    }

    @SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
    private static void onDigCompleted(Dog dog){
        LootTable lootTable = dog.level.getServer().getLootTables().get(COTWBuiltInLootTables.DOG_DIGGING);
        LootContext.Builder lcb = (new LootContext.Builder((ServerLevel)dog.level))
                .withParameter(LootContextParams.ORIGIN, dog.position())
                .withParameter(LootContextParams.THIS_ENTITY, dog)
                .withRandom(dog.getRandom());

        if(canBury(dog.getItemInMouth())){
            dog.setItemInMouth(ItemStack.EMPTY);
        }

        BlockPos digPos = DigAi.getDigLocation(dog).get().pos();

        boolean pickedUp = false;
        for(ItemStack giftStack : lootTable.getRandomItems(lcb.create(LootContextParamSets.GIFT))) {
            ItemEntity drop = new ItemEntity(dog.level, digPos.getX(), digPos.getY(), digPos.getY(), giftStack);
            dog.level.addFreshEntity(drop);
            if(!pickedUp){
                dog.pickUpItem(drop);
                fetchItem(dog);
                pickedUp = true;
            }
        }
    }

    protected static boolean canBury(ItemStack stack) {
        return stack.is(COTWTags.DOG_BURIES);
    }

    private static void fetchItem(LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        brain.eraseMemory(COTWMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get());
        brain.setMemory(COTWMemoryModuleTypes.FETCHING_ITEM.get(), true);
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getFetchPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new RunIf<>(DogGoalPackages::canFetch, new GoToWantedItem<>(DogGoalPackages::isNotHoldingItem, SPEED_MODIFIER_FETCHING, true, MAX_FETCH_DISTANCE)),
                        new RunIf<>(DogGoalPackages::canFetch, new GoToTargetAndGiveItem<>(Dog::getItemInMouth, SharedWolfAi::getOwnerPositionTracker, SPEED_MODIFIER_FETCHING, SharedWolfAi.CLOSE_ENOUGH_TO_FOLLOW_TARGET, 0, DogGoalPackages::onThrown), true),
                        new RunIf<>(DogGoalPackages::canFetch, new FollowOwner(SharedWolfAi::getOwnerPositionTracker, SPEED_MODIFIER_FETCHING, SharedWolfAi.CLOSE_ENOUGH_TO_FOLLOW_TARGET, SharedWolfAi.TOO_FAR_FROM_FOLLOW_TARGET)),
                        new StopItemActivityIfItemTooFarAway<>(DogGoalPackages::canStopFetchingIfItemTooFar, MAX_FETCH_DISTANCE, COTWMemoryModuleTypes.FETCHING_ITEM.get()),
                        new StopItemActivityIfTiredOfTryingToReachItem<>(DogGoalPackages::canGetTiredTryingToReachItem, MAX_TIME_TO_REACH_ITEM, DISABLE_FETCH_TIME, COTWMemoryModuleTypes.FETCHING_ITEM.get(), COTWMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get(), COTWMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get()),
                        new EraseMemoryIf<>(DogGoalPackages::wantsToStopFetching, COTWMemoryModuleTypes.FETCHING_ITEM.get())));
    }

    static boolean isNotHoldingItem(Dog dog) {
        return dog.getItemInMouth().isEmpty();
    }

    private static boolean canGetTiredTryingToReachItem(Dog dog) {
        return !dog.hasItemInMouth();
    }

    private static boolean canStopFetchingIfItemTooFar(Dog dog) {
        return !dog.hasItemInMouth();
    }

    private static boolean wantsToStopFetching(Dog dog) {
        return !dog.isTame() || (!dog.hasItemInMouth() && dog.isOnPickupCooldown());
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getIdlePackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new RunIf<>(SharedWolfAi::canFollowOwner, new FollowOwner(SharedWolfAi::getOwnerPositionTracker, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_FOLLOW_TARGET, SharedWolfAi.TOO_FAR_FROM_FOLLOW_TARGET), true),
                        new RunIf<>(SharedWolfAi::canMakeLove, new AnimalMakeLove(COTWEntityTypes.DOG.get(), SharedWolfAi.SPEED_MODIFIER_BREEDING), true),
                        new RunIf<>(SharedWolfAi::canFollowNonOwner, new FollowTemptation(SharedWolfAi::getSpeedModifierTempted), true),
                        new RunIf<>(SharedWolfAi::canFollowNonOwner, new BabyFollowAdult<>(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT)),
                        new RunIf<>(DogGoalPackages::canBeg, new Beg<>(DogGoalPackages::isInteresting, Dog::setIsInterested, SharedWolfAi.MAX_LOOK_DIST), true),
                        new StartAttacking<>(SharedWolfAi::canStartAttacking, SharedWolfAi::findNearestValidAttackTarget),
                        new StartHunting<>(DogGoalPackages::canHunt, SharedWolfAi::startHunting, SharedWolfAi.TIME_BETWEEN_HUNTS),
                        createIdleLookBehaviors(),
                        new RunIf<>(SharedWolfAi::canWander, createIdleMovementBehaviors(), true))
        );
    }

    public static boolean isInteresting(Dog dog, ItemStack stack) {
        return canFetch(stack) || canBury(stack) || dog.isFood(stack);
    }

    private static boolean canHunt(Dog dog){
        return !dog.isTame()
                && !AiUtil.hasAnyMemory(dog, MemoryModuleType.ANGRY_AT)
                && !dog.isBaby()
                && !HunterAi.hasAnyoneNearbyHuntedRecently(dog, GenericAi.getNearbyAdults(dog))
                && SharedWolfAi.canStartAttacking(dog);
    }

    private static boolean canBeg(Dog dog){
        return dog.isTame();
    }
}
