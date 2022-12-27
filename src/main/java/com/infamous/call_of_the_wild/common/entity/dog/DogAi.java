package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.behavior.Beg;
import com.infamous.call_of_the_wild.common.behavior.JumpAtTarget;
import com.infamous.call_of_the_wild.common.behavior.Retaliate;
import com.infamous.call_of_the_wild.common.behavior.dig.DigAtLocation;
import com.infamous.call_of_the_wild.common.behavior.hunter.RememberIfHuntTargetWasKilled;
import com.infamous.call_of_the_wild.common.behavior.hunter.StartHunting;
import com.infamous.call_of_the_wild.common.behavior.item.*;
import com.infamous.call_of_the_wild.common.behavior.pet.FollowOwner;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtByTarget;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtTarget;
import com.infamous.call_of_the_wild.common.behavior.pet.SitWhenOrderedTo;
import com.infamous.call_of_the_wild.common.registry.COTWActivities;
import com.infamous.call_of_the_wild.common.registry.COTWEntityTypes;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.AngerAi;
import com.infamous.call_of_the_wild.common.util.MiscUtil;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.infamous.call_of_the_wild.data.COTWBuiltInLootTables;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class DogAi {
    private static final float SPEED_MODIFIER_FETCHING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    public static final int DISABLE_FETCH_TIME = 200;
    public static final int ITEM_PICKUP_COOLDOWN = 60;
    public static final int MAX_FETCH_DISTANCE = 16;
    public static final int MAX_TIME_TO_REACH_ITEM = 200;
    private static final int START_FOLLOW_DISTANCE = 10;
    private static final long DIG_DURATION = 100L;

    protected static Brain<?> makeBrain(Brain<Dog> brain) {
        initCoreActivity(brain);
        initFightActivity(brain);
        initRetreatActivity(brain);
        initDiggingActivity(brain);
        initFetchActivity(brain);
        initIdleActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Dog> brain) {
        brain.addActivity(Activity.CORE, 0,
                ImmutableList.of(
                        new Swim(WolflikeAi.JUMP_CHANCE_IN_WATER),
                        new RunIf<>(WolflikeAi::shouldPanic, new AnimalPanic(WolflikeAi.SPEED_MODIFIER_PANICKING), true),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new SitWhenOrderedTo(),
                        new OwnerHurtByTarget(),
                        new OwnerHurtTarget(),
                        new CopyMemoryWithExpiry<>(
                                WolflikeAi::isNearDisliked,
                                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                                MemoryModuleType.AVOID_TARGET,
                                WolflikeAi.AVOID_DURATION),
                        new StopHoldingItemIfNoLongerInItemActivity<>(DogAi::canStopHolding, DogAi::stopHoldingItemInMouth, COTWMemoryModuleTypes.FETCHING_ITEM.get()),
                        new RunIf<>(DogAi::canFetch, new StartItemActivityWithItemIfSeen<>(DogAi::canFetch, COTWMemoryModuleTypes.FETCHING_ITEM.get(), COTWMemoryModuleTypes.FETCHING_DISABLED.get(), COTWMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get())),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS),
                        new Retaliate<>(DogAi::wasHurtBy),
                        new StopBeingAngryIfTargetDead<>()));
    }

    private static boolean canStopHolding(Dog dog) {
        return dog.hasItemInMouth() && !hasDigLocation(dog);
    }

    private static void stopHoldingItemInMouth(Dog dog) {
        ItemStack mouthStack = dog.getItemInMouth();
        dog.setItemInMouth(ItemStack.EMPTY);
        BehaviorUtils.throwItem(dog, mouthStack, GenericAi.getRandomNearbyPos(dog, 4, 2));
        onThrown(dog);
    }

    private static boolean canFetch(Dog dog, ItemEntity itemEntity){
        return canFetch(itemEntity.getItem()) && itemEntity.closerThan(dog, MAX_FETCH_DISTANCE);
    }

    private static boolean canFetch(Dog dog){
        return !dog.isOrderedToSit() && dog.isTame();
    }

    protected static boolean canFetch(ItemStack stack) {
        return stack.is(COTWTags.DOG_FETCHES);
    }

    private static void wasHurtBy(Dog dog, LivingEntity attacker) {
        if (dog.hasItemInMouth()) {
            stopHoldingItemInMouth(dog);
        }

        AiUtil.eraseAllMemories(dog,
                MemoryModuleType.BREED_TARGET,
                COTWMemoryModuleTypes.FETCHING_ITEM.get(),
                COTWMemoryModuleTypes.DIG_LOCATION.get());

        if (dog.isBaby()) {
            GenericAi.setAvoidTarget(dog, attacker, WolflikeAi.RETREAT_DURATION.sample(dog.level.random));
            if (Sensor.isEntityAttackableIgnoringLineOfSight(dog, attacker)) {
                AngerAi.broadcastAngerTarget(GenericAi.getNearbyAdults(dog).stream().map(Dog.class::cast).filter(d -> WolflikeAi.wantsToRetaliate(d, attacker)).toList(), attacker, WolflikeAi.ANGER_DURATION.sample(dog.getRandom()));
            }
        } else if(!dog.getBrain().isActive(Activity.AVOID)){
            AngerAi.maybeRetaliate(dog, GenericAi.getNearbyAdults(dog).stream().map(Dog.class::cast).filter(d -> WolflikeAi.wantsToRetaliate(d, attacker)).toList(), attacker, WolflikeAi.ANGER_DURATION.sample(dog.getRandom()), 4.0D);
        }
    }

    private static void initFightActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0,
                ImmutableList.of(
                        new StopAttackingIfTargetInvalid<>(),
                        new RunIf<>(WolflikeAi::canAttack, new SetWalkTargetFromAttackTargetIfTargetOutOfReach(WolflikeAi.SPEED_MODIFIER_CHASING)),
                        new RunIf<>(WolflikeAi::canAttack, new JumpAtTarget(), true),
                        new RunIf<>(WolflikeAi::canAttack, new MeleeAttack(WolflikeAi.ATTACK_COOLDOWN_TICKS)),
                        new RememberIfHuntTargetWasKilled<>(DogAi::isHuntTarget, WolflikeAi.TIME_BETWEEN_HUNTS),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)),
                MemoryModuleType.ATTACK_TARGET);
    }

    private static boolean isHuntTarget(Dog dog, LivingEntity target) {
        return AiUtil.isHuntTarget(dog, target, COTWTags.DOG_HUNT_TARGETS);
    }

    private static void initRetreatActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 0,
                ImmutableList.of(
                        new RunIf<>(WolflikeAi::canAvoid, SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, WolflikeAi.SPEED_MODIFIER_RETREATING, WolflikeAi.DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING, true)),
                        createIdleLookBehaviors(),
                        new RunIf<>(WolflikeAi::canWander, createIdleMovementBehaviors(), true),
                        new EraseMemoryIf<>(DogAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)),
                MemoryModuleType.AVOID_TARGET);
    }

    private static RunOne<Dog> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(COTWEntityTypes.DOG.get(), WolflikeAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, WolflikeAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(EntityType.VILLAGER, WolflikeAi.MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(WolflikeAi.MAX_LOOK_DIST), 1),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<Dog> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(WolflikeAi.SPEED_MODIFIER_WALKING), 2),
                        Pair.of(InteractWith.of(COTWEntityTypes.DOG.get(), WolflikeAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, WolflikeAi.SPEED_MODIFIER_WALKING, 2), 2),
                        Pair.of(InteractWith.of(EntityType.PLAYER, WolflikeAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, WolflikeAi.SPEED_MODIFIER_WALKING, 2), 2),
                        Pair.of(InteractWith.of(EntityType.VILLAGER, WolflikeAi.INTERACTION_RANGE, MemoryModuleType.INTERACTION_TARGET, WolflikeAi.SPEED_MODIFIER_WALKING, 2), 2),
                        Pair.of(new RunIf<>(GenericAi::doesntSeeAnyPlayerHoldingWantedItem, new SetWalkTargetFromLookTarget(WolflikeAi.SPEED_MODIFIER_WALKING, 3)), 2),
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

    private static void initDiggingActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.DIG,
                0,
                ImmutableList.of(
                        new RunIf<>(DogAi::canFetch, new GoToTargetLocation<>(COTWMemoryModuleTypes.DIG_LOCATION.get(), WolflikeAi.STOP_FOLLOW_DISTANCE, SPEED_MODIFIER_FETCHING)),
                        new RunIf<>(DogAi::canFetch, new DigAtLocation<>(DogAi::onDigCompleted, DIG_DURATION), true)),
                COTWMemoryModuleTypes.DIG_LOCATION.get());
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

        BlockPos digPos = dog.getBrain().getMemory(COTWMemoryModuleTypes.DIG_LOCATION.get()).get();

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

    private static void initFetchActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(COTWActivities.FETCH.get(), 0,
                ImmutableList.of(
                        new RunIf<>(DogAi::canFetch, new GoToWantedItem<>(DogAi::isNotHoldingItem, SPEED_MODIFIER_FETCHING, true, MAX_FETCH_DISTANCE)),
                        new RunIf<>(DogAi::canFetch, new GoToTargetAndGiveItem<>(Dog::getItemInMouth, DogAi::getOwnerPositionTracker, SPEED_MODIFIER_FETCHING, WolflikeAi.STOP_FOLLOW_DISTANCE, DogAi::onThrown), true),
                        new RunIf<>(DogAi::canFetch, new StayCloseToTarget<>(DogAi::getOwnerPositionTracker, WolflikeAi.STOP_FOLLOW_DISTANCE, WolflikeAi.STOP_FOLLOW_DISTANCE, SPEED_MODIFIER_FETCHING)),
                        new StopItemActivityIfItemTooFarAway<>(DogAi::canStopFetchingIfItemTooFar, MAX_FETCH_DISTANCE, COTWMemoryModuleTypes.FETCHING_ITEM.get()),
                        new StopItemActivityIfTiredOfTryingToReachItem<>(DogAi::canGetTiredTryingToReachItem, MAX_TIME_TO_REACH_ITEM, DISABLE_FETCH_TIME, COTWMemoryModuleTypes.FETCHING_ITEM.get(), COTWMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get(), COTWMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get()),
                        new EraseMemoryIf<>(DogAi::wantsToStopFetching, COTWMemoryModuleTypes.FETCHING_ITEM.get())),
                COTWMemoryModuleTypes.FETCHING_ITEM.get());
    }

    private static boolean isNotHoldingItem(Dog dog) {
        return dog.getItemInMouth().isEmpty();
    }

    private static Optional<PositionTracker> getOwnerPositionTracker(LivingEntity livingEntity) {
        if(livingEntity instanceof OwnableEntity ownable){
            Entity owner = ownable.getOwner();
            if(owner != null) return Optional.of(new EntityTracker(owner, true));
        }
        return Optional.empty();
    }

    private static void onThrown(Dog dog){
        dog.playSoundEvent(SoundEvents.FOX_SPIT);
        setItemPickupCooldown(dog);
    }

    private static void setItemPickupCooldown(Dog dog) {
        dog.getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, DogAi.ITEM_PICKUP_COOLDOWN);
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

    private static void initIdleActivity(Brain<Dog> brain) {
        brain.addActivity(Activity.IDLE, 0,
                ImmutableList.of(
                        new RunIf<>(WolflikeAi::canFollowOwner, new FollowOwner(WolflikeAi.SPEED_MODIFIER_WALKING, START_FOLLOW_DISTANCE, WolflikeAi.STOP_FOLLOW_DISTANCE), true),
                        new RunIf<>(WolflikeAi::canMakeLove, new AnimalMakeLove(COTWEntityTypes.DOG.get(), WolflikeAi.SPEED_MODIFIER_BREEDING), true),
                        new RunIf<>(WolflikeAi::canFollowNonOwner, new FollowTemptation(WolflikeAi::getSpeedModifierTempted), true),
                        new RunIf<>(WolflikeAi::canFollowNonOwner, new BabyFollowAdult<>(WolflikeAi.ADULT_FOLLOW_RANGE, WolflikeAi.SPEED_MODIFIER_FOLLOWING_ADULT)),
                        new RunIf<>(DogAi::canBeg, new Beg<>(DogAi::isInteresting, Dog::setIsInterested, WolflikeAi.MAX_LOOK_DIST), true),
                        new StartAttacking<>(WolflikeAi::canAttack, WolflikeAi::findNearestValidAttackTarget),
                        new StartHunting<>(DogAi::canHunt, WolflikeAi.TIME_BETWEEN_HUNTS),
                        createIdleLookBehaviors(),
                        new RunIf<>(WolflikeAi::canWander, createIdleMovementBehaviors(), true)
                ));
    }

    public static boolean isInteresting(Dog dog, ItemStack stack) {
        return canFetch(stack) || canBury(stack) || dog.isFood(stack);
    }

    private static boolean canHunt(Dog dog){
        return !dog.isBaby() && !dog.isTame() && WolflikeAi.canAttack(dog);
    }

    private static boolean canBeg(Dog dog){
        return dog.isTame();
    }

    /**
     * Called by {@link Dog#mobInteract(Player, InteractionHand)}
     */
    protected static InteractionResult mobInteract(Dog dog, Player player, InteractionHand hand, Supplier<InteractionResult> animalInteract) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        Level level = dog.level;

        if(dog.isTame()){
            if (!(item instanceof DyeItem dyeItem)) {
                if(canBury(stack) && !hasDigLocation(dog) && !hasDigCooldown(dog)){
                    Optional<BlockPos> digLocation = generateDigLocation(dog);
                    if(digLocation.isPresent()){
                        yieldAsPet(dog);
                        setDigLocation(dog, digLocation.get());
                        ItemStack singleton = stack.split(1);
                        holdInMouth(dog, singleton);
                        return InteractionResult.CONSUME;
                    } else{
                        return InteractionResult.PASS;
                    }
                }

                if(dog.isFood(stack) && dog.isInjured()){
                    dog.usePlayerItem(player, hand, stack);
                    return InteractionResult.CONSUME;
                }

                InteractionResult animalInteractResult = animalInteract.get(); // will set in breed mode if adult and not on cooldown, or age up if baby
                boolean willNotBreed = !animalInteractResult.consumesAction() || dog.isBaby();
                if (willNotBreed && dog.isOwnedBy(player)) {
                    dog.setOrderedToSit(!dog.isOrderedToSit());
                    dog.setJumping(false);
                    yieldAsPet(dog);
                    return InteractionResult.CONSUME;
                }

                return animalInteractResult;
            } else{
                DyeColor dyecolor = dyeItem.getDyeColor();
                if (dyecolor != dog.getCollarColor()) {
                    dog.setCollarColor(dyecolor);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }

                    return InteractionResult.CONSUME;
                }
            }
        } else if(dog.isFood(stack) && !dog.isAggressive()){
            dog.usePlayerItem(player, hand, stack);
            if (dog.getRandom().nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(dog, player)) {
                dog.tame(player);
                yieldAsPet(dog);
                dog.setOrderedToSit(true);
                level.broadcastEntityEvent(dog, WolflikeAi.SUCCESSFUL_TAME_ID);
            } else {
                level.broadcastEntityEvent(dog, WolflikeAi.FAILED_TAME_ID);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasDigLocation(Dog dog){
        return dog.getBrain().hasMemoryValue(COTWMemoryModuleTypes.DIG_LOCATION.get());
    }

    private static boolean hasDigCooldown(Dog dog){
        return dog.getBrain().hasMemoryValue(MemoryModuleType.DIG_COOLDOWN);
    }

    public static Optional<BlockPos> generateDigLocation(Dog dog){
        Vec3 randomPos = LandRandomPos.getPos(dog, 10, 7);
        if(randomPos == null) return Optional.empty();

        BlockPos blockPos = new BlockPos(randomPos);
        return Optional.of(blockPos).filter(bp -> dog.level.getBlockState(bp.below()).is(COTWTags.DOG_CAN_DIG));
    }

    private static void yieldAsPet(Dog dog) {
        GenericAi.stopWalking(dog);

        setItemPickupCooldown(dog);

        AiUtil.eraseAllMemories(dog,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.AVOID_TARGET,
                COTWMemoryModuleTypes.FETCHING_ITEM.get(),
                COTWMemoryModuleTypes.DIG_LOCATION.get());
    }

    private static void setDigLocation(Dog dog, BlockPos blockPos){
        dog.getBrain().setMemory(COTWMemoryModuleTypes.DIG_LOCATION.get(), blockPos);
    }

    private static void holdInMouth(Dog dog, ItemStack stack) {
        if (dog.hasItemInMouth()) {
            stopHoldingItemInMouth(dog);
        }

        dog.holdInMouth(stack);
    }

    /**
     * Called by {@link Dog#customServerAiStep()}
     */
    protected static void updateActivity(Dog dog) {
        Brain<Dog> brain = dog.getBrain();
        Activity previous = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.DIG, COTWActivities.FETCH.get(), Activity.IDLE));
        Activity current = brain.getActiveNonCoreActivity().orElse(null);

        if (previous != current) {
            getSoundForCurrentActivity(dog).ifPresent(dog::playSoundEvent);
        }

        dog.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        dog.setSprinting(AiUtil.hasAnyMemory(dog,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.AVOID_TARGET,
                MemoryModuleType.IS_PANICKING,
                COTWMemoryModuleTypes.FETCHING_ITEM.get(),
                COTWMemoryModuleTypes.DIG_LOCATION.get()));
    }

    protected static Optional<SoundEvent> getSoundForCurrentActivity(Dog dog) {
        return dog.getBrain().getActiveNonCoreActivity().map((a) -> getSoundForActivity(dog, a));
    }

    private static SoundEvent getSoundForActivity(Dog dog, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.WOLF_GROWL;
        } else if (activity == Activity.AVOID && GenericAi.isNearAvoidTarget(dog, WolflikeAi.DESIRED_DISTANCE_FROM_DISLIKED)) {
            return SoundEvents.WOLF_HURT;
        } else if (dog.getRandom().nextInt(3) == 0) {
            return dog.isTame() && dog.getHealth() < dog.getMaxHealth() * 0.5F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundEvents.WOLF_AMBIENT;
        }
    }

    /**
     * Called by {@link Dog#wantsToPickUp(ItemStack)}
     */
    protected static boolean wantsToPickup(Dog dog, ItemStack stack) {
        if (AiUtil.hasAnyMemory(dog,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.AVOID_TARGET,
                MemoryModuleType.IS_PANICKING,
                MemoryModuleType.BREED_TARGET)) {
            return false;
        } else if (canFetch(stack)) {
            return isNotHoldingItem(dog) && dog.isTame();
        }
        return false;
    }

    /**
     * Called by {@link Dog#pickUpItem(ItemEntity)}
     */
    protected static void pickUpItem(Dog dog, ItemEntity itemEntity) {
        dog.take(itemEntity, 1);
        ItemStack singleton = MiscUtil.removeOneItemFromItemEntity(itemEntity);
        holdInMouth(dog, singleton);
    }

}
