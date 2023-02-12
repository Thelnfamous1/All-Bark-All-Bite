package com.infamous.all_bark_all_bite.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.ai.*;
import com.infamous.all_bark_all_bite.common.behavior.*;
import com.infamous.all_bark_all_bite.common.behavior.dig.DigAtLocation;
import com.infamous.all_bark_all_bite.common.behavior.item.GiveItemToTarget;
import com.infamous.all_bark_all_bite.common.behavior.item.StartItemActivityWithItemIfSeen;
import com.infamous.all_bark_all_bite.common.behavior.item.StopItemActivityIfItemTooFarAway;
import com.infamous.all_bark_all_bite.common.behavior.item.StopItemActivityIfTiredOfTryingToReachItem;
import com.infamous.all_bark_all_bite.common.behavior.sleep.WakeUpTrigger;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfBrain;
import com.infamous.all_bark_all_bite.common.registry.ABABActivities;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.data.ABABBuiltInLootTables;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public class DogBrain {

    public static Brain<Dog> makeBrain(Brain<Dog> brain) {
        BrainMaker<Dog> brainMaker = new BrainMaker<>(brain);
        brainMaker.initActivityWithConditions(Activity.PANIC,
                SharedWolfBrain.getPanicPackage(), SharedWolfBrain.getPanicConditions());
        brainMaker.initActivityWithConditions(ABABActivities.SIT.get(),
                SharedWolfBrain.getSitPackage(createIdleLookBehaviors(), beg()), SharedWolfBrain.getSitConditions());
        brainMaker.initActivityWithMemoryGate(ABABActivities.POUNCE.get(),
                SharedWolfBrain.getPouncePackage(), ABABMemoryModuleTypes.POUNCE_TARGET.get());
        brainMaker.initActivityWithMemoryGate(Activity.FIGHT,
                SharedWolfBrain.getFightPackage(DogBrain::isHuntTarget), MemoryModuleType.ATTACK_TARGET);
        brainMaker.initActivityWithMemoryGate(Activity.AVOID,
                SharedWolfBrain.getAvoidPackage(DogBrain::wantsToStopFleeing, createIdleMovementBehaviors(), createIdleLookBehaviors()), MemoryModuleType.AVOID_TARGET);
        brainMaker.initActivityWithMemoryGate(ABABActivities.STALK.get(),
                SharedWolfBrain.getStalkPackage(), ABABMemoryModuleTypes.STALK_TARGET.get());
        brainMaker.initActivityWithMemoryGate(Activity.DIG,
                getDigPackage(), ABABMemoryModuleTypes.DIG_LOCATION.get());
        brainMaker.initActivityWithMemoryGate(ABABActivities.FETCH.get(),
                getFetchPackage(), ABABMemoryModuleTypes.FETCHING_ITEM.get());
        brainMaker.initActivityWithConditions(Activity.REST,
                SharedWolfBrain.getRestPackage(createIdleLookBehaviors()), SharedWolfBrain.getRestConditions());
        brainMaker.initActivity(Activity.IDLE,
                getIdlePackage());

        brainMaker.initCoreActivity(Activity.CORE,
                getCorePackage());
        brainMaker.initCoreActivity(ABABActivities.COUNT_DOWN.get(),
                getCountDownPackage());
        brainMaker.initCoreActivity(ABABActivities.TARGET.get(),
                SharedWolfBrain.getTargetPackage(DogBrain::wasHurtBy, DogBrain::canStartHunting, DogBrain::canStartStalking));
        brainMaker.initCoreActivity(ABABActivities.UPDATE.get(),
                SharedWolfBrain.getUpdatePackage(brainMaker.getActivities(), DogBrain::onActivityChanged));

        return brainMaker.makeBrain(Activity.IDLE);
    }

    @NotNull
    private static Beg<Dog> beg() {
        return new Beg<>(DogAi::isInteresting, Dog::setIsInterested, SharedWolfAi.MAX_LOOK_DIST);
    }

    private static boolean isHuntTarget(Dog dog, LivingEntity target) {
        return target.getType().is(ABABTags.DOG_HUNT_TARGETS);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static boolean wantsToStopFleeing(TamableAnimal tamableAnimal) {
        if(tamableAnimal.isTame()) return true;

        Brain<?> brain = tamableAnimal.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        } else {
            LivingEntity avoidTarget = brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
            if (DogAi.isDisliked(avoidTarget)) {
                return !brain.isMemoryValue(ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), avoidTarget);
            } else {
                return false;
            }
        }
    }

    private static void wasHurtBy(Dog dog, LivingEntity attacker) {
        AiUtil.eraseMemories(dog,
                MemoryModuleType.BREED_TARGET,
                ABABMemoryModuleTypes.STALK_TARGET.get(),
                ABABMemoryModuleTypes.POUNCE_TARGET.get(),
                ABABMemoryModuleTypes.FETCHING_ITEM.get(),
                ABABMemoryModuleTypes.DIG_LOCATION.get());

        SharedWolfAi.reactToAttack(dog, attacker);
    }

    private static boolean canStartHunting(Dog dog){
        return !dog.isTame() && SharedWolfBrain.canStartHunting(dog);
    }

    private static boolean canStartStalking(Dog dog){
        return (!dog.isTame() || dog.isBaby()) && SharedWolfBrain.canStartStalking(dog);
    }

    private static void onActivityChanged(Dog dog, Pair<Activity, Activity> ignoredActivityChange){
        DogAi.getSoundForCurrentActivity(dog).ifPresent(se -> AiUtil.playSoundEvent(dog, se));
    }

    private static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getCorePackage(){
        return BrainUtil.createPriorityPairs(0, ImmutableList.of(
                new HurtByTrigger<>(SharedWolfBrain::onHurtBy),
                new WakeUpTrigger<>(SharedWolfAi::wantsToWakeUp),
                new AgeChangeTrigger<>(SharedWolfBrain::onAgeChanged),
                new Swim(SharedWolfAi.JUMP_CHANCE_IN_WATER),
                new RunIf<>(SharedWolfAi::shouldPanic, new AnimalPanic(SharedWolfAi.SPEED_MODIFIER_PANICKING), true),
                new RunIf<>(TamableAnimal::isTame, new StartItemActivityWithItemIfSeen<>(DogBrain::canFetchItemEntity, ABABMemoryModuleTypes.FETCHING_ITEM.get(), ABABMemoryModuleTypes.FETCHING_DISABLED.get(), ABABMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get())),
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
                new UpdateUnitMemory<>(SharedWolfAi::isInDayTime, ABABMemoryModuleTypes.IS_LEVEL_DAY.get())
        ));
    }

    private static boolean canFetchItemEntity(Dog dog, ItemEntity itemEntity){
        return Util.mapNullable(itemEntity.getThrower(), uuid -> uuid.equals(dog.getOwnerUUID())) != null
                && itemEntity.getItem().is(ABABTags.DOG_FETCHES)
                && itemEntity.closerThan(dog, SharedWolfAi.MAX_FETCH_DISTANCE);
    }

    private static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getCountDownPackage(){
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(ABABMemoryModuleTypes.POUNCE_COOLDOWN_TICKS.get()),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)
                ));
    }

    private static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getDigPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(DogBrain::canSprintDig),
                        new StayCloseToTarget<>(DogBrain::getDigPosition, 0, 1, SharedWolfAi.SPEED_MODIFIER_FETCHING),
                        new DigAtLocation<>(DogBrain::onDigCompleted, SharedWolfAi.DIG_DURATION)
                ));
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

        if(DogAi.canBury(dog.getMainHandItem())){
            dog.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        BlockPos digPos = DigAi.getDigLocation(dog).get().pos();

        boolean pickedUp = false;
        for(ItemStack giftStack : lootTable.getRandomItems(lcb.create(LootContextParamSets.GIFT))) {
            if(!pickedUp){
                SharedWolfAi.holdInMouth(dog, giftStack.split(1));
                SharedWolfBrain.fetchItem(dog);
                if(!giftStack.isEmpty()){
                    AiUtil.dropItemAtPos(dog, digPos, giftStack);
                }
                pickedUp = true;
            } else{
                AiUtil.dropItemAtPos(dog, digPos, giftStack);
            }
        }
    }

    private static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getFetchPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove),
                        SharedWolfAi.createGoToWantedItem(true),
                        new GiveItemToTarget<>(LivingEntity::getMainHandItem, AiUtil::getOwner, SharedWolfAi.CLOSE_ENOUGH_TO_OWNER, DogBrain::onItemThrown),
                        new RunIf<>(DogBrain::canReturnItemToOwner, SharedWolfAi.createFollowOwner(SharedWolfAi.SPEED_MODIFIER_FETCHING), true),
                        new StopItemActivityIfItemTooFarAway<>(DogBrain::isNotHoldingItem, SharedWolfAi.MAX_FETCH_DISTANCE, ABABMemoryModuleTypes.FETCHING_ITEM.get()),
                        new StopItemActivityIfTiredOfTryingToReachItem<>(DogBrain::isNotHoldingItem, SharedWolfAi.MAX_TIME_TO_REACH_ITEM, SharedWolfAi.DISABLE_FETCH_TIME, ABABMemoryModuleTypes.FETCHING_ITEM.get(), ABABMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get(), ABABMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get()),
                        new EraseMemoryIf<>(DogBrain::wantsToStopFetching, ABABMemoryModuleTypes.FETCHING_ITEM.get())));
    }

    static void onItemThrown(Dog dog){
        AiUtil.playSoundEvent(dog, SoundEvents.FOX_SPIT);
        AiUtil.setItemPickupCooldown(dog, SharedWolfAi.ITEM_PICKUP_COOLDOWN);
    }

    private static boolean canReturnItemToOwner(Dog dog){
        return dog.isTame() && !isNotHoldingItem(dog);
    }

    private static boolean isNotHoldingItem(Dog dog) {
        return dog.getMainHandItem().isEmpty();
    }

    private static boolean wantsToStopFetching(Dog dog) {
        return !dog.isTame() || (isNotHoldingItem(dog) && GenericAi.isOnPickupCooldown(dog));
    }

    private static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super Dog>>> getIdlePackage(){
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove, SharedWolfAi.TOO_FAR_FROM_WALK_TARGET),
                        new RunIf<>(SharedWolfBrain::isFollowingOwner, SharedWolfAi.createFollowOwner(SharedWolfAi.SPEED_MODIFIER_WALKING), true),
                        BrainUtil.tryAllBehaviorsInOrderIfAbsent(
                                ImmutableList.of(
                                        new FollowTemptation(SharedWolfAi::getSpeedModifierTempted),
                                        SharedWolfBrain.createBreedBehavior(ABABEntityTypes.DOG.get()),
                                        new RunIf<>(SharedWolfAi::wantsToFindShelter, new MoveToNonSkySeeingSpot(SharedWolfAi.SPEED_MODIFIER_WALKING), true),
                                        new BabyFollowAdult<>(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT)
                                ),
                                ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get(),
                                ABABMemoryModuleTypes.IS_ORDERED_TO_HEEL.get()
                        ),
                        SharedWolfAi.createGoToWantedItem(false),
                        createIdleMovementBehaviors(),
                        beg(),
                        createIdleLookBehaviors()
                ));
    }

    private static RunOne<TamableAnimal> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(ABABEntityTypes.DOG.get(), SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<TamableAnimal> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(SharedWolfAi.SPEED_MODIFIER_WALKING), 3),
                        Pair.of(InteractWith.of(ABABEntityTypes.DOG.get(), SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(InteractWith.of(EntityType.PLAYER, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(InteractWith.of(EntityType.VILLAGER, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(new RunIf<>(Predicate.not(GenericAi::seesPlayerHoldingWantedItem), new SetWalkTargetFromLookTarget(SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_LOOK_TARGET)), 2),
                        Pair.of(new RunIf<>(Predicate.not(SharedWolfAi::alertable), new PerchAndSearch<>(TamableAnimal::isInSittingPose, TamableAnimal::setInSittingPose), true), 2),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

}
