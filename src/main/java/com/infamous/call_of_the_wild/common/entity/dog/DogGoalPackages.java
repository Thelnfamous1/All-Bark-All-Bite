package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.ai.*;
import com.infamous.call_of_the_wild.common.behavior.*;
import com.infamous.call_of_the_wild.common.behavior.dig.DigAtLocation;
import com.infamous.call_of_the_wild.common.behavior.hunter.RememberIfHuntTargetWasKilled;
import com.infamous.call_of_the_wild.common.behavior.hunter.StartHunting;
import com.infamous.call_of_the_wild.common.behavior.item.*;
import com.infamous.call_of_the_wild.common.behavior.pet.*;
import com.infamous.call_of_the_wild.common.behavior.sleep.StartSleeping;
import com.infamous.call_of_the_wild.common.behavior.sleep.WakeUpTrigger;
import com.infamous.call_of_the_wild.common.entity.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.ABABEntityTypes;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.data.ABABBuiltInLootTables;
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
import java.util.function.Predicate;

public class DogGoalPackages {

    public static final int ITEM_PICKUP_COOLDOWN = 60;
    public static final int MAX_FETCH_DISTANCE = 16;
    public static final int DISABLE_FETCH_TIME = 200;
    public static final int MAX_TIME_TO_REACH_ITEM = 200;
    private static final long DIG_DURATION = 100L;

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getCorePackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Swim(SharedWolfAi.JUMP_CHANCE_IN_WATER),
                        new HurtByTrigger<>(DogGoalPackages::wasHurtBy),
                        new WakeUpTrigger<>(SharedWolfAi::wantsToWakeUp),
                        new RunIf<>(SharedWolfAi::shouldPanic, new AnimalPanic(SharedWolfAi.SPEED_MODIFIER_PANICKING), true),
                        new Sprint<>(SharedWolfAi::canSprintCore),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new SitWhenOrderedTo(),
                        //new StopSittingToWalk(),
                        new OwnerHurtByTarget<>(SharedWolfAi::canDefendOwner, SharedWolfAi::wantsToAttack),
                        new OwnerHurtTarget<>(SharedWolfAi::canDefendOwner, TamableAnimal::wantsToAttack),
                        new CopyMemoryWithExpiry<>(
                                SharedWolfAi::isNearDisliked,
                                ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                                MemoryModuleType.AVOID_TARGET,
                                SharedWolfAi.AVOID_DURATION),
                        new RunIf<>(DogGoalPackages::canFetch, new StartItemActivityWithItemIfSeen<>(DogGoalPackages::canFetch, ABABMemoryModuleTypes.FETCHING_ITEM.get(), ABABMemoryModuleTypes.FETCHING_DISABLED.get(), ABABMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get())),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS),
                        new StopBeingAngryIfTargetDead<>()));
    }

    private static boolean canFetch(Dog dog, ItemEntity itemEntity){
        return dog.getMainHandItem().isEmpty() && canFetch(itemEntity.getItem()) && itemEntity.closerThan(dog, MAX_FETCH_DISTANCE);
    }

    static boolean canFetch(TamableAnimal tamableAnimal){
        return SharedWolfAi.canMove(tamableAnimal) && tamableAnimal.isTame();
    }

    protected static boolean canFetch(ItemStack stack) {
        return stack.is(ABABTags.DOG_FETCHES);
    }

    private static void wasHurtBy(Dog dog, LivingEntity attacker) {
        if (!dog.getMainHandItem().isEmpty()) {
            SharedWolfAi.stopHoldingItemInMouth(dog);
        }

        dog.setOrderedToSit(false);
        SharedWolfAi.clearStates(dog);

        AiUtil.eraseMemories(dog,
                MemoryModuleType.BREED_TARGET,
                ABABMemoryModuleTypes.FETCHING_ITEM.get(),
                ABABMemoryModuleTypes.DIG_LOCATION.get());

        SharedWolfAi.reactToAttack(dog, attacker);
    }

    static void onItemThrown(Dog dog){
        dog.playSoundEvent(SoundEvents.FOX_SPIT);
        AiUtil.setItemPickupCooldown(dog, ITEM_PICKUP_COOLDOWN);
    }

    @NotNull
    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getFightPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove),
                        new StopAttackingIfTargetInvalid<>(),
                        new RunIf<>(SharedWolfAi::canStartAttacking, new SetWalkTargetFromAttackTargetIfTargetOutOfReach(SharedWolfAi.SPEED_MODIFIER_CHASING)),
                        new RunIf<>(SharedWolfAi::canStartAttacking, new LeapAtTarget(SharedWolfAi.LEAP_YD, SharedWolfAi.TOO_CLOSE_TO_LEAP, SharedWolfAi.POUNCE_DISTANCE), true),
                        new RunIf<>(SharedWolfAi::canStartAttacking, new MeleeAttack(SharedWolfAi.ATTACK_COOLDOWN_TICKS)),
                        new RememberIfHuntTargetWasKilled<>(DogGoalPackages::isHuntTarget, SharedWolfAi.TIME_BETWEEN_HUNTS),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)));
    }

    private static boolean isHuntTarget(Dog dog, LivingEntity target) {
        return target.getType().is(ABABTags.DOG_HUNT_TARGETS);
    }

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getAvoidPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove),
                        new RunIf<>(Predicate.not(TamableAnimal::isTame), SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, SharedWolfAi.SPEED_MODIFIER_RETREATING, SharedWolfAi.DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING, true)),
                        createIdleLookBehaviors(),
                        new RunIf<>(DogGoalPackages::canWander, createIdleMovementBehaviors(), true),
                        new EraseMemoryIf<>(DogGoalPackages::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)));
    }

    static RunOne<Dog> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(ABABEntityTypes.DOG.get(), SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    static RunOne<Dog> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(SharedWolfAi.SPEED_MODIFIER_WALKING), 2),
                        Pair.of(InteractWith.of(ABABEntityTypes.DOG.get(), SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(InteractWith.of(EntityType.PLAYER, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(InteractWith.of(EntityType.VILLAGER, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(new RunIf<>(Predicate.not(GenericAi::seesPlayerHoldingWantedItem), new SetWalkTargetFromLookTarget(SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_LOOK_TARGET)), 2),
                        Pair.of(new RunIf<>(Predicate.not(SharedWolfAi::alertable), new PerchAndSearch<>(TamableAnimal::isInSittingPose, TamableAnimal::setInSittingPose), true), 2),
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
                return !brain.isMemoryValue(ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), avoidTarget);
            } else {
                return false;
            }
        }
    }

    private static boolean wantsToAvoid(EntityType<?> entityType) {
        return entityType.is(ABABTags.DOG_DISLIKED);
    }

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getDigPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(DogGoalPackages::canSprintDig),
                        new RunIf<>(DogGoalPackages::canFetch, new StayCloseToTarget<>(DogGoalPackages::getDigPosition, 0, 1, SharedWolfAi.SPEED_MODIFIER_FETCHING)),
                        new RunIf<>(DogGoalPackages::canFetch, new DigAtLocation<>(DogGoalPackages::onDigCompleted, DIG_DURATION), true)));
    }

    private static boolean canSprintDig(Dog dog){
        return SharedWolfAi.canMove(dog) && !dog.hasPose(Pose.DIGGING);
    }

    private static Optional<PositionTracker> getDigPosition(LivingEntity dog) {
        Brain<?> brain = dog.getBrain();
        Optional<GlobalPos> digLocation = DigAi.getDigLocation(dog);
        if (digLocation.isPresent()) {
            GlobalPos digPos = digLocation.get();
            Level level = dog.getLevel();
            if (level.dimension() == digPos.dimension() && level.getBlockState(digPos.pos().below()).is(ABABTags.DOG_CAN_DIG)) {
                return Optional.of(new BlockPosTracker(digPos.pos()));
            }

            brain.eraseMemory(ABABMemoryModuleTypes.DIG_LOCATION.get());
        }

        return Optional.empty();
    }

    @SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
    private static void onDigCompleted(Dog dog){
        LootTable lootTable = dog.level.getServer().getLootTables().get(ABABBuiltInLootTables.DOG_DIGGING);
        LootContext.Builder lcb = (new LootContext.Builder((ServerLevel)dog.level))
                .withParameter(LootContextParams.ORIGIN, dog.position())
                .withParameter(LootContextParams.THIS_ENTITY, dog)
                .withRandom(dog.getRandom());

        if(canBury(dog.getMainHandItem())){
            dog.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        BlockPos digPos = DigAi.getDigLocation(dog).get().pos();

        boolean pickedUp = false;
        for(ItemStack giftStack : lootTable.getRandomItems(lcb.create(LootContextParamSets.GIFT))) {
            if(!pickedUp){
                SharedWolfAi.holdInMouth(dog, giftStack.split(1));
                fetchItem(dog);
                if(!giftStack.isEmpty()){
                    dropItemAtPos(dog, digPos, giftStack);
                }
                pickedUp = true;
            } else{
                dropItemAtPos(dog, digPos, giftStack);
            }
        }
    }

    private static void dropItemAtPos(Dog dog, BlockPos blockPos, ItemStack itemStack) {
        ItemEntity drop = new ItemEntity(dog.level, blockPos.getX(), blockPos.getY(), blockPos.getY(), itemStack);
        dog.level.addFreshEntity(drop);
    }

    protected static boolean canBury(ItemStack stack) {
        return stack.is(ABABTags.DOG_BURIES);
    }

    private static void fetchItem(LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        brain.eraseMemory(ABABMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get());
        brain.setMemory(ABABMemoryModuleTypes.FETCHING_ITEM.get(), true);
    }

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getFetchPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove),
                        SharedWolfAi.createGoToWantedItem(DogGoalPackages::canFetch, true),
                        new RunIf<>(DogGoalPackages::canFetch, new GiveItemToTarget<>(LivingEntity::getMainHandItem, AiUtil::getOwner, SharedWolfAi.CLOSE_ENOUGH_TO_OWNER, DogGoalPackages::onItemThrown), true),
                        new RunIf<>(DogGoalPackages::canReturnItemToOwner, createFollowOwner(SharedWolfAi.SPEED_MODIFIER_FETCHING), true),
                        new StopItemActivityIfItemTooFarAway<>(DogGoalPackages::canStopFetchingIfItemTooFar, MAX_FETCH_DISTANCE, ABABMemoryModuleTypes.FETCHING_ITEM.get()),
                        new StopItemActivityIfTiredOfTryingToReachItem<>(DogGoalPackages::canGetTiredTryingToReachItem, MAX_TIME_TO_REACH_ITEM, DISABLE_FETCH_TIME, ABABMemoryModuleTypes.FETCHING_ITEM.get(), ABABMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get(), ABABMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get()),
                        new EraseMemoryIf<>(DogGoalPackages::wantsToStopFetching, ABABMemoryModuleTypes.FETCHING_ITEM.get())));
    }

    private static FollowOwner<Dog> createFollowOwner(float speedModifier) {
        return new FollowOwner<>(DogGoalPackages::dontFollowIf, AiUtil::getOwner, speedModifier, SharedWolfAi.CLOSE_ENOUGH_TO_OWNER);
    }

    private static boolean dontFollowIf(Dog dog){
        return dog.isOrderedToSit();
    }

    private static boolean canReturnItemToOwner(Dog dog){
        return canFetch(dog) && !dog.getMainHandItem().isEmpty();
    }

    private static boolean canGetTiredTryingToReachItem(Dog dog) {
        return dog.getMainHandItem().isEmpty();
    }

    private static boolean canStopFetchingIfItemTooFar(Dog dog) {
        return dog.getMainHandItem().isEmpty();
    }

    private static boolean wantsToStopFetching(Dog dog) {
        return !dog.isTame() || (dog.getMainHandItem().isEmpty() && GenericAi.isOnPickupCooldown(dog));
    }

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getRestPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new EraseMemoryIf<>(Predicate.not(LivingEntity::isSleeping), ABABMemoryModuleTypes.IS_SLEEPING.get())
                ));
    }

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getIdlePackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove, SharedWolfAi.TOO_FAR_FROM_WALK_TARGET),
                        new RunIf<>(SharedWolfAi::canMove, new AnimalMakeLove(ABABEntityTypes.DOG.get(), SharedWolfAi.SPEED_MODIFIER_BREEDING), true),
                        new StartAttacking<>(SharedWolfAi::canStartAttacking, SharedWolfAi::findNearestValidAttackTarget),
                        new StartHunting<>(DogGoalPackages::canHunt, SharedWolfAi::startHunting, SharedWolfAi.TIME_BETWEEN_HUNTS),
                        createInactiveBehaviors())
        );
    }

    @SuppressWarnings("unchecked")
    private static GateBehavior<Dog> createInactiveBehaviors() {
        return BrainUtil.tryAllBehaviorsInOrderIfAbsent(
                BrainUtil.basicWeightedBehaviors(
                        new RunIf<>(DogGoalPackages::wantsToFindShelter, new MoveToNonSkySeeingSpot(SharedWolfAi.SPEED_MODIFIER_WALKING)),
                        new StartSleeping<>(DogGoalPackages::canSleep),
                        createAwakeBehaviors()
                ),
                MemoryModuleType.BREED_TARGET,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.ANGRY_AT);
    }

    private static boolean wantsToFindShelter(Dog dog){
        return canWander(dog) && SharedWolfAi.wantsToFindShelter(dog);
    }

    private static boolean canSleep(Dog dog){
        return !CommandAi.isFollowing(dog) && SharedWolfAi.canSleep(dog);
    }

    @SuppressWarnings("unchecked")
    private static GateBehavior<Dog> createAwakeBehaviors() {
        return BrainUtil.tryAllBehaviorsInOrderIfAbsent(
                BrainUtil.basicWeightedBehaviors(
                        new RunIf<>(DogGoalPackages::canFollowOwner, createFollowOwner(SharedWolfAi.SPEED_MODIFIER_WALKING), true),
                        new RunIf<>(Predicate.not(TamableAnimal::isTame), new FollowTemptation(SharedWolfAi::getSpeedModifierTempted), true),
                        new RunIf<>(Predicate.not(TamableAnimal::isTame), new BabyFollowAdult<>(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT)),
                        createIdleBehaviors()
                ),
                ABABMemoryModuleTypes.IS_SLEEPING.get()
        );
    }

    @SuppressWarnings("unchecked")
    private static GateBehavior<Dog> createIdleBehaviors() {
        return BrainUtil.tryAllBehaviorsInOrderIfAbsent(
                BrainUtil.basicWeightedBehaviors(
                        new Beg<>(DogGoalPackages::isInteresting, Dog::setIsInterested, SharedWolfAi.MAX_LOOK_DIST),
                        SharedWolfAi.createGoToWantedItem(DogGoalPackages::canWander, false),
                        createIdleLookBehaviors(),
                        new RunIf<>(DogGoalPackages::canWander, createIdleMovementBehaviors(), true),
                        new Eat(SharedWolfAi::setAteRecently, SharedWolfAi.EAT_DURATION)
                ),
                MemoryModuleType.WALK_TARGET
        );
    }

    public static boolean isInteresting(Dog dog, ItemStack stack) {
        return canFetch(stack) || canBury(stack) || dog.isFood(stack);
    }

    private static boolean canHunt(Dog dog){
        return !dog.isTame()
                && !AngerAi.hasAngryAt(dog)
                && !dog.isBaby()
                && !HunterAi.hasAnyoneNearbyHuntedRecently(dog, GenericAi.getNearbyAdults(dog))
                && SharedWolfAi.canStartAttacking(dog);
    }

    public static boolean canFollowOwner(Dog dog) {
        return !BehaviorUtils.isBreeding(dog) && CommandAi.isFollowing(dog);
    }

    static boolean canWander(Dog dog){
        return !dog.isSleeping() && (!dog.isInSittingPose() || !dog.isOrderedToSit()) && !CommandAi.isFollowing(dog);
    }
}
