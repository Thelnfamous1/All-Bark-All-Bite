package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.behavior.*;
import com.infamous.call_of_the_wild.common.registry.COTWEntityTypes;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.registry.COTWSensorTypes;
import com.infamous.call_of_the_wild.common.util.AiHelper;
import com.infamous.call_of_the_wild.data.COTWBuiltInLootTables;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DogAi {
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final UniformInt ANGER_DURATION = TimeUtil.rangeOfSeconds(20, 39); // same as Wolf's persistent anger time
    private static final UniformInt AVOID_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    private static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
    public static final Collection<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,

            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,

            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,

            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.UNIVERSAL_ANGER,

            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,

            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,

            MemoryModuleType.AVOID_TARGET,

            MemoryModuleType.BREED_TARGET,
            COTWMemoryModuleTypes.NEARBY_ADULTS.get(),
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            COTWMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),

            MemoryModuleType.TEMPTING_PLAYER,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
            MemoryModuleType.IS_TEMPTED,

            COTWMemoryModuleTypes.DIG_LOCATION.get(),
            MemoryModuleType.DIG_COOLDOWN,

            MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
            COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get(),
            COTWMemoryModuleTypes.TIME_TRYING_TO_REACH_PLAY_ITEM.get(),
            COTWMemoryModuleTypes.DISABLE_WALK_TO_PLAY_ITEM.get(),
            MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
            COTWMemoryModuleTypes.OWNER.get(),

            MemoryModuleType.HAS_HUNTING_COOLDOWN,
            MemoryModuleType.IS_PANICKING
    );
    public static final Collection<? extends SensorType<? extends Sensor<? super Dog>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES, // NEAREST_LIVING_ENTITIES, NEAREST_VISIBLE_LIVING_ENTITIES
            SensorType.NEAREST_PLAYERS, // NEAREST_PLAYERS, NEAREST_VISIBLE_PLAYER, NEAREST_VISIBLE_ATTACKABLE_PLAYER
            SensorType.NEAREST_ITEMS, // NEAREST_VISIBLE_WANTED_ITEM
            SensorType.NEAREST_ADULT, // NEAREST_VISIBLE_ADULT
            COTWSensorTypes.NEAREST_ADULTS.get(), // NEARBY_ADULTS, NEAREST_VISIBLE_ADULTS
            SensorType.HURT_BY, // HURT_BY, HURT_BY_ENTITY
            COTWSensorTypes.DOG_TEMPTATIONS.get(),  // TEMPTING_PLAYER
            COTWSensorTypes.DOG_SPECIFIC_SENSOR.get()); // NEAREST_PLAYER_HOLDING_WANTED_ITEM, NEAREST_ATTACKABLE, NEAREST_VISIBLE_DISLIKED
    private static final List<Activity> ACTIVITIES = ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.DIG, Activity.PLAY, Activity.IDLE);
    private static final float JUMP_CHANCE_IN_WATER = 0.8F;
    private static final float SPEED_MODIFIER_BREEDING = 1.0F;
    private static final float SPEED_MODIFIER_CHASING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    private static final float SPEED_MODIFIER_FOLLOWING_ADULT = 1.0F;
    private static final float SPEED_MODIFIER_PANICKING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    private static final float SPEED_MODIFIER_RETREATING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    private static final float SPEED_MODIFIER_TEMPTED = 1.0F;
    private static final float SPEED_MODIFIER_WALKING = 1.0F;
    private static final float SPEED_MODIFIER_PLAYING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private static final int DESIRED_DISTANCE_FROM_DISLIKED = 6;
    private static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
    private static final int MAX_LOOK_DIST = 8;
    private static final int START_FOLLOW_DISTANCE = 10;
    private static final int STOP_FOLLOW_DISTANCE = 2;
    private static final byte SUCCESSFUL_TAME_ID = 7;
    private static final byte FAILED_TAME_ID = 6;
    public static final int MAX_FETCH_DISTANCE = 16;
    public static final int ITEM_PICKUP_COOLDOWN = 60;
    public static final int MAX_TIME_TO_REACH_ITEM = 200;
    public static final int DISABLE_PLAY_TIME = 200;
    private static final long DIG_DURATION = 100L;

    public static Ingredient getTemptations() {
        return CompoundIngredient.of(Ingredient.of(COTWTags.DOG_FOOD), Ingredient.of(COTWTags.DOG_LOVED));
    }

    protected static boolean isFood(ItemStack stack) {
        return stack.is(COTWTags.DOG_FOOD);
    }

    protected static boolean isLoved(ItemStack stack) {
        return stack.is(COTWTags.DOG_LOVED);
    }
    protected static boolean isBuriable(ItemStack stack) {
        return stack.is(COTWTags.DOG_BURIES);
    }
    protected static Brain<?> makeBrain(Brain<Dog> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        initRetreatActivity(brain);
        initPlayActivity(brain);
        initDiggingActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Dog> brain) {
        brain.addActivity(Activity.CORE, 0,
                ImmutableList.of(
                        new Swim(JUMP_CHANCE_IN_WATER),
                        new RunIf<>(DogAi::shouldPanic, new AnimalPanic(SPEED_MODIFIER_PANICKING), true),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new SitWhenOrderedTo(),
                        new OwnerHurtByTarget(),
                        new OwnerHurtTarget(),
                        new CopyMemoryWithExpiry<>(
                                DogAi::isNearDisliked,
                                COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(),
                                MemoryModuleType.AVOID_TARGET,
                                AVOID_DURATION),
                        new StopHoldingItemIfNoLongerPlaying<>(DogAi::canStopHolding, DogAi::stopHoldingItemInMouth),
                        new RunIf<>(DogAi::canPlay, new StartPlayingWithItemIfSeen<>(DogAi::isLoved)),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS),
                        new StopBeingAngryIfTargetDead<>()));
    }

    private static boolean canStopHolding(Dog dog) {
        return dog.hasItemInMouth() && !hasDigLocation(dog);
    }

    private static boolean isNearDisliked(Dog dog) {
        return AiHelper.isNearDisliked(dog, DESIRED_DISTANCE_FROM_DISLIKED);
    }

    private static boolean shouldPanic(Dog dog) {
        return dog.isFreezing() || dog.isOnFire();
    }

    private static void initIdleActivity(Brain<Dog> brain) {
        brain.addActivity(Activity.IDLE, 0,
                ImmutableList.of(
                        new RunIf<>(DogAi::canFollowOwner, new FollowOwner(SPEED_MODIFIER_WALKING, START_FOLLOW_DISTANCE, STOP_FOLLOW_DISTANCE), true),
                        new RunIf<>(DogAi::canMakeLove, new AnimalMakeLove(COTWEntityTypes.DOG.get(), SPEED_MODIFIER_BREEDING), true),
                        new RunIf<>(DogAi::canFollowNonOwner, new RunOne<>(
                                ImmutableList.of(
                                        Pair.of(new FollowTemptation(DogAi::getSpeedModifierTempted), 1),
                                        Pair.of(new BabyFollowAdult<>(ADULT_FOLLOW_RANGE, SPEED_MODIFIER_FOLLOWING_ADULT), 1))
                        ), true),
                        new RunIf<>(DogAi::canBeg, new Beg<>(MAX_LOOK_DIST), true),
                        createIdleLookBehaviors(),
                        new RunIf<>(DogAi::canWander, createIdleMovementBehaviors(), true),
                        new StartAttacking<>(DogAi::canAttack, DogAi::findNearestValidAttackTarget)));
    }

    private static boolean canFollowOwner(Dog dog) {
        return !BehaviorUtils.isBreeding(dog);
    }

    private static boolean canMakeLove(Dog dog){
        return !dog.isOrderedToSit();
    }

    private static boolean canFollowNonOwner(Dog dog) {
        return !dog.isTame();
    }

    private static float getSpeedModifierTempted(LivingEntity dog) {
        return SPEED_MODIFIER_TEMPTED;
    }

    private static boolean canWander(Dog dog){
        return !dog.isOrderedToSit();
    }

    private static RunSometimes<Dog> createIdleLookBehaviors() {
        return new RunSometimes<>(
                new SetEntityLookTarget(EntityType.PLAYER, MAX_LOOK_DIST),
                UniformInt.of(30, 60));
    }

    private static boolean canBeg(Dog dog){
        return dog.isTame();
    }

    private static RunOne<Dog> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(SPEED_MODIFIER_WALKING), 2),
                        Pair.of(new SetWalkTargetFromLookTarget(SPEED_MODIFIER_WALKING, 3), 2),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    private static boolean canAttack(Dog dog) {
        return !dog.isOrderedToSit() && !BehaviorUtils.isBreeding(dog);
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Dog dog) {
        Brain<Dog> brain = dog.getBrain();
        Optional<LivingEntity> angryAt = BehaviorUtils.getLivingEntityFromUUIDMemory(dog, MemoryModuleType.ANGRY_AT);
        if (angryAt.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(dog, angryAt.get())) {
            return angryAt;
        } else {
            if (brain.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
                Optional<Player> player = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
                if (player.isPresent()) {
                    return player;
                }
            }

            return brain.getMemory(MemoryModuleType.NEAREST_ATTACKABLE);
        }
    }

    private static void initFightActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 0,
                ImmutableList.of(
                        new StopAttackingIfTargetInvalid<>(),
                        new RunIf<>(DogAi::canAttack, new SetWalkTargetFromAttackTargetIfTargetOutOfReach(SPEED_MODIFIER_CHASING)),
                        new RunIf<>(DogAi::canAttack, new LeapAtTarget(), true),
                        new RunIf<>(DogAi::canAttack, new MeleeAttack(ATTACK_COOLDOWN_TICKS)),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)),
                MemoryModuleType.ATTACK_TARGET);
    }



    private static void initRetreatActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 0,
                ImmutableList.of(
                        new RunIf<>(DogAi::canAvoid, SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, SPEED_MODIFIER_RETREATING, DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING, false)),
                        createIdleLookBehaviors(),
                        new RunIf<>(DogAi::canWander, createIdleMovementBehaviors(), true),
                        new EraseMemoryIf<>(DogAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)),
                MemoryModuleType.AVOID_TARGET);
    }

    private static boolean canAvoid(Dog dog){
        return !dog.isTame();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static boolean wantsToStopFleeing(Dog dog) {
        if(dog.isTame()) return true;

        Brain<Dog> brain = dog.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        } else {
            LivingEntity avoidTarget = brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
            EntityType<?> avoidType = avoidTarget.getType();
            if (wantsToAvoid(avoidType)) {
                return !brain.isMemoryValue(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), avoidTarget);
            } else {
                return !dog.isBaby();
            }
        }
    }

    public static boolean wantsToAvoid(EntityType<?> entityType) {
        return entityType.is(COTWTags.DOG_DISLIKED);
    }

    private static void initPlayActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.PLAY, 0,
                ImmutableList.of(
                        new RunIf<>(DogAi::canPlay, new GoToWantedItem<>(DogAi::isNotHoldingWantedItem, SPEED_MODIFIER_PLAYING, true, MAX_FETCH_DISTANCE)),
                        new RunIf<>(DogAi::canPlay, new GoToTargetAndGiveItem<>(Dog::getItemInMouth, DogAi::getOwnerPositionTracker, SPEED_MODIFIER_PLAYING, STOP_FOLLOW_DISTANCE, ITEM_PICKUP_COOLDOWN, DogAi::onThrown), true),
                        new RunIf<>(DogAi::canPlay, new StayCloseToTarget<>(DogAi::getOwnerPositionTracker, STOP_FOLLOW_DISTANCE, STOP_FOLLOW_DISTANCE + 1, SPEED_MODIFIER_PLAYING)),
                        new StopPlayingIfItemTooFarAway<>(DogAi::canStopPlayingIfItemTooFar, MAX_FETCH_DISTANCE),
                        new StopPlayingIfTiredOfTryingToReachItem<>(DogAi::canGetTiredTryingToReachItem, MAX_TIME_TO_REACH_ITEM, DISABLE_PLAY_TIME),
                        new EraseMemoryIf<>(DogAi::wantsToStopPlaying, COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get())),
                COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get());
    }

    public static void onThrown(Dog dog){
        dog.playSoundEvent(SoundEvents.FOX_SPIT);
    }

    private static boolean canPlay(Dog dog){
        return !dog.isOrderedToSit() && dog.isTame();
    }

    private static boolean wantsToStopPlaying(Dog dog) {
        return !dog.isTame() || (!dog.hasItemInMouth() && dog.getBrain().hasMemoryValue(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS));
    }

    private static boolean isNotHoldingWantedItem(Dog dog) {
        return dog.getItemInMouth().isEmpty(); //|| !isLoved(dog.getItemInMouth());
    }

    private static Optional<PositionTracker> getOwnerPositionTracker(LivingEntity livingEntity) {
        return getOwner(livingEntity).map(le -> new EntityTracker(le, true));
    }

    private static Optional<LivingEntity> getOwner(LivingEntity livingEntity) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(livingEntity, COTWMemoryModuleTypes.OWNER.get());
    }

    private static boolean canStopPlayingIfItemTooFar(Dog dog) {
        return !dog.hasItemInMouth();
    }

    private static boolean canGetTiredTryingToReachItem(Dog dog) {
        return !dog.hasItemInMouth();
    }

    private static void initDiggingActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.DIG,
                0,
                ImmutableList.of(
                        new RunIf<>(DogAi::canPlay, new GoToTargetLocation<>(COTWMemoryModuleTypes.DIG_LOCATION.get(), STOP_FOLLOW_DISTANCE, SPEED_MODIFIER_PLAYING)),
                        new RunIf<>(DogAi::canPlay, new DigAtLocation<>(DogAi::onDigStarted, DogAi::onDigCompleted, DogAi::onDigStopped, DIG_DURATION), true)),
                COTWMemoryModuleTypes.DIG_LOCATION.get());
    }

    private static void onDigStarted(Dog dog){
    }

    @SuppressWarnings("ConstantConditions")
    private static void onDigCompleted(Dog dog){
        RandomSource random = dog.getRandom();
        LootTable lootTable = dog.level.getServer().getLootTables().get(COTWBuiltInLootTables.DOG_DIGGING);
        LootContext.Builder lcb = (new LootContext.Builder((ServerLevel)dog.level))
                .withParameter(LootContextParams.ORIGIN, dog.position())
                .withParameter(LootContextParams.THIS_ENTITY, dog)
                .withRandom(random);

        if(isBuriable(dog.getItemInMouth())){
            dog.setItemInMouth(ItemStack.EMPTY);
        }

        boolean pickedUp = false;
        for(ItemStack giftStack : lootTable.getRandomItems(lcb.create(LootContextParamSets.GIFT))) {
            ItemEntity drop = new ItemEntity(dog.level,
                    dog.getX() - (double) Mth.sin(dog.yBodyRot * ((float) Math.PI / 180F)),
                    dog.getY(),
                    dog.getZ() + (double) Mth.cos(dog.yBodyRot * ((float) Math.PI / 180F)), giftStack);
            dog.level.addFreshEntity(drop);
            if(!pickedUp){
                dog.pickUpItem(drop);
                pickedUp = true;
            }
        }
    }

    private static void onDigStopped(Dog dog){
        //dog.getBrain().setMemoryWithExpiry(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 200L);
    }

    protected static InteractionResult mobInteract(Dog dog, Player player, InteractionHand hand, Supplier<InteractionResult> animalInteract) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        Level level = dog.level;

        if(dog.isTame()){
            if (!(item instanceof DyeItem dyeItem)) {
                if(isBuriable(stack) && !hasDigLocation(dog) && !hasDigCooldown(dog)){
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
                level.broadcastEntityEvent(dog, SUCCESSFUL_TAME_ID);
            } else {
                level.broadcastEntityEvent(dog, FAILED_TAME_ID);
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
        //CallOfTheWild.LOGGER.info("Generated dig location for {} at {}", dog, blockPos);
        return Optional.of(blockPos).filter(bp -> dog.level.getBlockState(bp.below()).is(COTWTags.DOG_DIGS_ON));
    }

    private static void setDigLocation(Dog dog, BlockPos blockPos){
        //CallOfTheWild.LOGGER.info("Set dig location for {} at {}", dog, blockPos);
        dog.getBrain().setMemory(COTWMemoryModuleTypes.DIG_LOCATION.get(), blockPos);
    }

    private static void yieldAsPet(Dog dog) {
        AiHelper.stopWalking(dog);
        AiHelper.eraseAllMemories(dog,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.AVOID_TARGET,
                COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get(),
                COTWMemoryModuleTypes.DIG_LOCATION.get());
    }

    protected static void updateActivity(Dog dog) {
        Brain<Dog> brain = dog.getBrain();
        Activity previous = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ACTIVITIES);
        Activity current = brain.getActiveNonCoreActivity().orElse(null);
        if (previous == Activity.FIGHT && current != Activity.FIGHT) {
            brain.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, TIME_BETWEEN_HUNTS.sample(dog.getRandom()));
        }
        if (previous != current) {
            getSoundForCurrentActivity(dog).ifPresent(dog::playSoundEvent);
        }

        dog.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        dog.setSprinting(AiHelper.hasAnyMemory(dog,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.AVOID_TARGET,
                MemoryModuleType.IS_PANICKING,
                COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get(),
                COTWMemoryModuleTypes.DIG_LOCATION.get()));
    }

    protected static Optional<SoundEvent> getSoundForCurrentActivity(Dog dog) {
        return dog.getBrain().getActiveNonCoreActivity().map((a) -> getSoundForActivity(dog, a));
    }

    private static SoundEvent getSoundForActivity(Dog dog, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.WOLF_GROWL;
        } else if (activity == Activity.AVOID && AiHelper.isNearAvoidTarget(dog, DESIRED_DISTANCE_FROM_DISLIKED)) {
            return SoundEvents.WOLF_HURT;
        }else if (dog.getRandom().nextInt(3) == 0) {
            return dog.isTame() && dog.getHealth() < dog.getMaxHealth() * 0.5F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundEvents.WOLF_AMBIENT;
        }
    }

    protected static void wasHurtBy(Dog dog, LivingEntity attacker) {
        Brain<Dog> brain = dog.getBrain();

        if (dog.hasItemInMouth()) {
            stopHoldingItemInMouth(dog);
        }

        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        brain.eraseMemory(COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get());
        brain.eraseMemory(COTWMemoryModuleTypes.DIG_LOCATION.get());


        if (dog.isBaby()) {
            AiHelper.setAvoidTarget(dog, attacker, RETREAT_DURATION.sample(dog.level.random));
            if (Sensor.isEntityAttackableIgnoringLineOfSight(dog, attacker)) {
                AiHelper.broadcastAngerTarget(AiHelper.getNearbyAdults(dog), attacker, ANGER_DURATION.sample(dog.getRandom()));
            }
        } else {
            AiHelper.maybeRetaliate(dog, AiHelper.getNearbyAdults(dog), attacker, ANGER_DURATION.sample(dog.getRandom()));
        }
    }

    protected static boolean wantsToPickup(Dog dog, ItemStack stack) {
        if (AiHelper.hasAnyMemory(dog, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.AVOID_TARGET, MemoryModuleType.BREED_TARGET)) {
            return false;
        } else if (isLoved(stack)) {
            return isNotHoldingWantedItem(dog) && dog.isTame();
        }
        return false;
    }

    protected static void pickUpItem(Dog dog, ItemEntity itemEntity) {
        dog.take(itemEntity, 1);
        ItemStack singleton = removeOneItemFromItemEntity(itemEntity);
        dog.getBrain().eraseMemory(COTWMemoryModuleTypes.TIME_TRYING_TO_REACH_PLAY_ITEM.get());
        holdInMouth(dog, singleton);
        playWithItem(dog);
    }

    private static void playWithItem(LivingEntity livingEntity) {
        livingEntity.getBrain().setMemory(COTWMemoryModuleTypes.PLAYING_WITH_ITEM.get(), true);
    }

    private static void holdInMouth(Dog dog, ItemStack stack) {
        if (dog.hasItemInMouth()) {
            stopHoldingItemInMouth(dog);
        }

        dog.holdInMouth(stack);
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        ItemStack singleton = stack.split(1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }

        return singleton;
    }

    protected static void stopHoldingItemInMouth(Dog dog) {
        ItemStack mouthStack = dog.getItemInMouth();
        dog.setItemInMouth(ItemStack.EMPTY);
        BehaviorUtils.throwItem(dog, mouthStack, getRandomNearbyPos(dog));
        onThrown(dog);
    }

    private static Vec3 getRandomNearbyPos(PathfinderMob pathfinderMob) {
        Vec3 vec3 = LandRandomPos.getPos(pathfinderMob, 4, 2);
        return vec3 == null ? pathfinderMob.position() : vec3;
    }

}
