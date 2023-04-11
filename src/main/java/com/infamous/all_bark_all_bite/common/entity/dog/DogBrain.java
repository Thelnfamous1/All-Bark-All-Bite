package com.infamous.all_bark_all_bite.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.behavior.dig.DigAtLocation;
import com.infamous.all_bark_all_bite.common.behavior.item.GiveItemToTarget;
import com.infamous.all_bark_all_bite.common.behavior.item.StartItemActivityWithItemIfSeen;
import com.infamous.all_bark_all_bite.common.behavior.item.StopItemActivityIfItemTooFarAway;
import com.infamous.all_bark_all_bite.common.behavior.item.StopItemActivityIfTiredOfTryingToReachItem;
import com.infamous.all_bark_all_bite.common.behavior.misc.*;
import com.infamous.all_bark_all_bite.common.behavior.pet.Beg;
import com.infamous.all_bark_all_bite.common.behavior.sleep.MoveToNonSkySeeingSpot;
import com.infamous.all_bark_all_bite.common.behavior.sleep.WakeUpTrigger;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfBrain;
import com.infamous.all_bark_all_bite.common.logic.BrainMaker;
import com.infamous.all_bark_all_bite.common.registry.ABABActivities;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.util.ai.BrainUtil;
import com.infamous.all_bark_all_bite.common.util.ai.DigAi;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import com.infamous.all_bark_all_bite.data.ABABBuiltInLootTables;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Optional;
import java.util.function.Predicate;

public class DogBrain {

    public static Brain<Dog> makeBrain(Brain<Dog> brain) {
        BrainMaker<Dog> brainMaker = new BrainMaker<>(brain);
        brainMaker.initActivityWithConditions(Activity.PANIC,
                SharedWolfBrain.getPanicPackage(), SharedWolfBrain.getPanicConditions());
        brainMaker.initActivityWithConditions(ABABActivities.SIT.get(),
                SharedWolfBrain.getSitPackage(createIdleLookBehaviors(), beg()), SharedWolfBrain.getSitConditions());
        brainMaker.initActivityWithMemoryGate(Activity.FIGHT,
                SharedWolfBrain.getFightPackage(DogBrain::isHuntTarget), MemoryModuleType.ATTACK_TARGET);
        brainMaker.initActivityWithMemoryGate(Activity.AVOID,
                SharedWolfBrain.getAvoidPackage(createIdleMovementBehaviors(), createIdleLookBehaviors()), MemoryModuleType.AVOID_TARGET);
        brainMaker.initActivityWithMemoryGate(Activity.DIG,
                getDigPackage(), ABABMemoryModuleTypes.DIG_LOCATION.get());
        brainMaker.initActivityWithMemoryGate(ABABActivities.FETCH.get(),
                getFetchPackage(), ABABMemoryModuleTypes.FETCHING_ITEM.get());
        brainMaker.initActivityWithConditions(ABABActivities.FOLLOW.get(),
                SharedWolfBrain.getFollowPackage(createIdleMovementBehaviors(), beg(), createIdleLookBehaviors()), SharedWolfBrain.getFollowConditions());
        brainMaker.initActivityWithMemoryGate(ABABActivities.HUNT.get(),
                SharedWolfBrain.getHuntPackage(), ABABMemoryModuleTypes.HUNT_TARGET.get());
        brainMaker.initActivityWithConditions(Activity.REST,
                SharedWolfBrain.getRestPackage(createIdleLookBehaviors(), false), SharedWolfBrain.getRestConditions(ABABMemoryModuleTypes.IS_LEVEL_NIGHT.get()));
        brainMaker.initActivity(Activity.IDLE,
                getIdlePackage());

        brainMaker.initCoreActivity(Activity.CORE,
                getCorePackage());
        brainMaker.initCoreActivity(ABABActivities.COUNT_DOWN.get(),
                SharedWolfBrain.getCountDownPackage());
        brainMaker.initCoreActivity(ABABActivities.TARGET.get(),
                SharedWolfBrain.getTargetPackage(DogBrain::wasHurtBy, DogBrain::canStartHunting));
        brainMaker.initCoreActivity(ABABActivities.UPDATE.get(),
                SharedWolfBrain.getUpdatePackage(brainMaker.getActivities(), DogBrain::onActivityChanged));

        return brainMaker.makeBrain(Activity.IDLE);
    }

    private static Beg<Dog> beg() {
        return new Beg<>(DogAi::isInteresting, Dog::setIsInterested, SharedWolfAi.MAX_LOOK_DIST);
    }

    private static boolean isHuntTarget(Dog dog, LivingEntity target) {
        return target.getType().is(ABABTags.DOG_HUNT_TARGETS);
    }

    private static void wasHurtBy(Dog dog, LivingEntity attacker) {
        AiUtil.eraseMemories(dog,
                MemoryModuleType.BREED_TARGET,
                ABABMemoryModuleTypes.HUNT_TARGET.get(),
                ABABMemoryModuleTypes.FETCHING_ITEM.get(),
                ABABMemoryModuleTypes.DIG_LOCATION.get());

        SharedWolfAi.reactToAttack(dog, attacker);
    }

    private static boolean canStartHunting(Dog dog){
        return !dog.isTame() && SharedWolfAi.canStartAttacking(dog);
    }

    private static void onActivityChanged(Dog dog, Pair<Activity, Activity> ignoredActivityChange){
        DogAi.getSoundForCurrentActivity(dog).ifPresent(se -> AiUtil.playSoundEvent(dog, se));
    }

    private static ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Dog>>> getCorePackage(){
        return BrainUtil.createPriorityPairs(0, ImmutableList.of(
                new HurtByTrigger<>(SharedWolfBrain::onHurtBy),
                new WakeUpTrigger<>(SharedWolfAi::wantsToWakeUp),
                new Swim(SharedWolfAi.JUMP_CHANCE_IN_WATER),
                SharedWolfBrain.createAnimalPanic(),
                BehaviorBuilder.triggerIf(TamableAnimal::isTame, StartItemActivityWithItemIfSeen.create(DogBrain::canFetchItemEntity, ABABMemoryModuleTypes.FETCHING_ITEM.get(), ABABMemoryModuleTypes.FETCHING_DISABLED.get(), ABABMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get())),
                SharedWolfBrain.createLookAtTargetSink(),
                SharedWolfBrain.createMoveToTargetSink(),
                SharedWolfBrain.copyToAvoidTarget(ABABMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get()),
                new UpdateUnitMemory<>(TamableAnimal::isOrderedToSit, ABABMemoryModuleTypes.IS_ORDERED_TO_SIT.get()),
                new UpdateUnitMemory<>(SharedWolfAi::hasShelter, ABABMemoryModuleTypes.IS_SHELTERED.get()),
                new UpdateUnitMemory<>(SharedWolfAi::isInNightTime, ABABMemoryModuleTypes.IS_LEVEL_NIGHT.get()),
                new UpdateUnitMemory<>(DogBrain::isAlert, ABABMemoryModuleTypes.IS_ALERT.get()),
                new UpdateUnitMemory<>(LivingEntity::isSleeping, ABABMemoryModuleTypes.IS_SLEEPING.get())
        ));
    }

    private static boolean isAlert(Dog dog) {
        return SharedWolfAi.alertable(dog, ABABTags.DOG_HUNT_TARGETS, ABABTags.DOG_ALWAYS_HOSTILES, ABABTags.DOG_DISLIKED);
    }

    private static boolean canFetchItemEntity(Dog dog, ItemEntity itemEntity){
        Entity itemEntityOwner = itemEntity.getOwner();
        return itemEntityOwner != null
                && itemEntityOwner == dog.getOwner()
                && itemEntity.getItem().is(ABABTags.DOG_FETCHES)
                && itemEntity.closerThan(dog, SharedWolfAi.MAX_FETCH_DISTANCE);
    }

    private static ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Dog>>> getDigPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(DogBrain::canSprintWhileDigging),
                        StayCloseToTarget.create(DogBrain::getDigPosition, le -> true, 0, 1, SharedWolfAi.SPEED_MODIFIER_FETCHING),
                        new DigAtLocation<>(DogBrain::onDigCompleted, SharedWolfAi.DIG_DURATION)
                ));
    }

    private static boolean canSprintWhileDigging(Dog dog){
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

    private static ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Dog>>> getFetchPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove),
                        SharedWolfAi.createGoToWantedItem(true),
                        new GiveItemToTarget<>(LivingEntity::getMainHandItem, AiUtil::getOwner, SharedWolfAi.CLOSE_ENOUGH_TO_OWNER, DogBrain::onItemThrown),
                        new RunBehaviorIf<>(DogBrain::canReturnItemToOwner, SharedWolfAi.createFollowOwner(SharedWolfAi.SPEED_MODIFIER_FETCHING)),
                        new StopItemActivityIfItemTooFarAway<>(DogBrain::isNotHoldingItem, SharedWolfAi.MAX_FETCH_DISTANCE, ABABMemoryModuleTypes.FETCHING_ITEM.get()),
                        new StopItemActivityIfTiredOfTryingToReachItem<>(DogBrain::isNotHoldingItem, SharedWolfAi.MAX_TIME_TO_REACH_ITEM, SharedWolfAi.DISABLE_FETCH_TIME, ABABMemoryModuleTypes.FETCHING_ITEM.get(), ABABMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get(), ABABMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get()),
                        EraseMemoryIf.create(DogBrain::wantsToStopFetching, ABABMemoryModuleTypes.FETCHING_ITEM.get())));
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

    private static ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Dog>>> getIdlePackage(){
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi::canMove, SharedWolfAi.TOO_FAR_FROM_WALK_TARGET),
                        new Eat(SharedWolfAi::setAteRecently),
                        new FollowTemptation(SharedWolfAi::getSpeedModifierTempted),
                        SharedWolfBrain.createBreedBehavior(ABABEntityTypes.DOG.get()),
                        BehaviorBuilder.triggerIf(livingEntity -> SharedWolfAi.wantsToFindShelter(livingEntity, false), MoveToNonSkySeeingSpot.create(SharedWolfAi.SPEED_MODIFIER_WALKING)),
                        BabyFollowAdult.create(SharedWolfAi.ADULT_FOLLOW_RANGE, SharedWolfAi.SPEED_MODIFIER_FOLLOWING_ADULT),
                        SharedWolfBrain.babySometimesHuntBaby(),
                        new PlayTagWithOtherBabies(SharedWolfAi.SPEED_MODIFIER_RETREATING, SharedWolfAi.SPEED_MODIFIER_CHASING),
                        SharedWolfAi.createGoToWantedItem(false),
                        new PerchAndSearch<>(SharedWolfBrain::canPerch, TamableAnimal::setInSittingPose),
                        createIdleMovementBehaviors(),
                        beg(),
                        createIdleLookBehaviors()
        ));
    }

    private static RunOne<Dog> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(SetEntityLookTarget.create(ABABEntityTypes.DOG.get(), SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(SetEntityLookTarget.create(EntityType.VILLAGER, SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(SetEntityLookTarget.create(SharedWolfAi.MAX_LOOK_DIST), 1),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<Dog> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(RandomStroll.stroll(SharedWolfAi.SPEED_MODIFIER_WALKING), 3),
                        Pair.of(InteractWith.of(ABABEntityTypes.DOG.get(), SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(InteractWith.of(EntityType.PLAYER, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(InteractWith.of(EntityType.VILLAGER, SharedWolfAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_INTERACT), 2),
                        Pair.of(BehaviorBuilder.triggerIf(Predicate.not(GenericAi::seesPlayerHoldingWantedItem), SetWalkTargetFromLookTarget.create(SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_LOOK_TARGET)), 2),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

}
