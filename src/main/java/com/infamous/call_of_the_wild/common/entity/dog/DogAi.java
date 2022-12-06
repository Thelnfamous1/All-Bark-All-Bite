package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.behavior.*;
import com.infamous.call_of_the_wild.common.registry.COTWEntityTypes;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.registry.COTWSensorTypes;
import com.infamous.call_of_the_wild.common.util.AiHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DogAi {
    private static final Ingredient DOG_FOOD = Ingredient.of(COTWTags.DOG_FOOD);
    private static final Ingredient DOG_LOVED = Ingredient.of(COTWTags.DOG_LOVED);
    private static final Ingredient DOG_TEMPTATIONS = CompoundIngredient.of(DOG_FOOD, DOG_LOVED);
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final UniformInt ANGER_DURATION = TimeUtil.rangeOfSeconds(30, 30);
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
            //MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,

            MemoryModuleType.HAS_HUNTING_COOLDOWN
    );
    public static final Collection<? extends SensorType<? extends Sensor<? super Dog>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES, // NEAREST_LIVING_ENTITIES, NEAREST_VISIBLE_LIVING_ENTITIES
            SensorType.NEAREST_PLAYERS, // NEAREST_PLAYERS, NEAREST_VISIBLE_PLAYER, NEAREST_VISIBLE_ATTACKABLE_PLAYER
            //SensorType.NEAREST_ITEMS, // NEAREST_VISIBLE_WANTED_ITEM
            SensorType.NEAREST_ADULT, // NEAREST_VISIBLE_ADULT
            COTWSensorTypes.NEAREST_ADULTS.get(), // NEARBY_ADULTS, NEAREST_VISIBLE_ADULTS
            SensorType.HURT_BY, // HURT_BY, HURT_BY_ENTITY
            COTWSensorTypes.DOG_TEMPTATIONS.get(),  // TEMPTING PLAYER
            COTWSensorTypes.DOG_SPECIFIC_SENSOR.get()); // NEAREST_PLAYER_HOLDING_WANTED_ITEM, NEAREST_ATTACKABLE, NEAREST_VISIBLE_DISLIKED
    private static final List<Activity> ACTIVITIES = ImmutableList.of(Activity.FIGHT, Activity.AVOID, Activity.IDLE);
    private static final double LEAP_Y_DELTA = 0.4D;
    private static final float JUMP_CHANCE_IN_WATER = 0.8F;
    private static final float SPEED_MODIFIER_BREEDING = 1.0F;
    private static final float SPEED_MODIFIER_CHASING = 0.77F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.0F
    private static final float SPEED_MODIFIER_FOLLOWING_ADULT = 1.0F;
    private static final float SPEED_MODIFIER_PANICKING = 1.154F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.5F
    private static final float SPEED_MODIFIER_RETREATING = 1.154F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.5F
    private static final float SPEED_MODIFIER_TEMPTED = 1.0F;
    private static final float SPEED_MODIFIER_WALKING = 1.0F;
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private static final int DESIRED_DISTANCE_FROM_DISLIKED = 6;
    private static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
    private static final int MAX_LOOK_DIST = 8;
    private static final int START_FOLLOW_DISTANCE = 10;
    private static final int STOP_FOLLOW_DISTANCE = 2;
    private static final int TARGET_DISTANCE_DIFFERENCE_THRESHOLD = 4;
    private static final byte SUCCESSFUL_TAME_ID = 7;
    private static final byte FAILED_TAME_ID = 6;

    public static Ingredient getTemptations() {
        return DOG_TEMPTATIONS;
    }

    protected static boolean isFood(ItemStack stack) {
        return DOG_FOOD.test(stack);
    }

    protected static boolean isLoved(ItemStack stack) {
        return DOG_LOVED.test(stack);
    }
    protected static Brain<?> makeBrain(Brain<Dog> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        initRetreatActivity(brain);
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
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new StopBeingAngryIfTargetDead<>()));
    }

    private static boolean isNearDisliked(Dog dog) {
        if(dog.isTame()) return false;

        Brain<Dog> brain = dog.getBrain();
        if (brain.hasMemoryValue(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get())) {
            LivingEntity disliked = brain.getMemory(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get()).get();
            return dog.closerThan(disliked, DESIRED_DISTANCE_FROM_DISLIKED);
        } else {
            return false;
        }
    }

    private static boolean shouldPanic(Dog dog) {
        return dog.isFreezing() || dog.isOnFire();
    }

    private static void initIdleActivity(Brain<Dog> brain) {
        brain.addActivity(Activity.IDLE, 0,
                ImmutableList.of(
                        new FollowOwner(SPEED_MODIFIER_WALKING, START_FOLLOW_DISTANCE, STOP_FOLLOW_DISTANCE),
                        new RunIf<>(Dog::isMobile, new AnimalMakeLove(COTWEntityTypes.DOG.get(), SPEED_MODIFIER_BREEDING), true),
                        new RunIf<>(TamableAnimal::isTame, new Beg<>(MAX_LOOK_DIST), true),
                        new RunIf<>(Dog::isWild, new FollowTemptation(DogAi::getSpeedModifierTempted), true),
                        new RunIf<>(Dog::isWild, new BabyFollowAdult<>(ADULT_FOLLOW_RANGE, SPEED_MODIFIER_FOLLOWING_ADULT)),
                        createIdleLookBehaviors(),
                        new RunIf<>(Dog::isMobile, createIdleMovementBehaviors(), true),
                        new StartAttacking<>(DogAi::canAttack, DogAi::findNearestValidAttackTarget)));
    }

    private static RunOne<Dog> createIdleLookBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new SetEntityLookTarget(EntityType.PLAYER, MAX_LOOK_DIST), 1),
                        Pair.of(new SetEntityLookTarget(MAX_LOOK_DIST), 1),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    private static RunOne<Dog> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(SPEED_MODIFIER_WALKING), 2),
                        Pair.of(new SetWalkTargetFromLookTarget(SPEED_MODIFIER_WALKING, 3), 2),
                        Pair.of(new DoNothing(30, 60), 1)));
    }

    private static float getSpeedModifierTempted(LivingEntity dog) {
        return SPEED_MODIFIER_TEMPTED;
    }

    private static boolean canAttack(Dog dog) {
        return dog.isMobile() && !BehaviorUtils.isBreeding(dog);
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
                        new RunIf<>(Dog::isMobile, new SetWalkTargetFromAttackTargetIfTargetOutOfReach(SPEED_MODIFIER_CHASING)),
                        new RunIf<>(Dog::isMobile, new LeapAtTarget(), true),
                        new RunIf<>(Dog::isMobile, new MeleeAttack(ATTACK_COOLDOWN_TICKS)),
                        new EraseMemoryIf<>(BehaviorUtils::isBreeding, MemoryModuleType.ATTACK_TARGET)),
                MemoryModuleType.ATTACK_TARGET);
    }

    private static void initRetreatActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 0,
                ImmutableList.of(
                        new RunIf<>(Dog::isMobile, SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, SPEED_MODIFIER_RETREATING, DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING, false)),
                        createIdleLookBehaviors(),
                        new RunIf<>(Dog::isMobile, createIdleMovementBehaviors(), true),
                        new EraseMemoryIf<>(DogAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)),
                MemoryModuleType.AVOID_TARGET);
    }

    private static boolean wantsToStopFleeing(Dog dog) {
        if(dog.isTame()) return true;

        Brain<Dog> brain = dog.getBrain();
        if (!brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            return true;
        } else {
            LivingEntity avoidTarget = brain.getMemory(MemoryModuleType.AVOID_TARGET).get();
            EntityType<?> type = avoidTarget.getType();
            if (wantsToAvoid(type)) {
                return !brain.isMemoryValue(COTWMemoryModuleTypes.NEAREST_VISIBLE_DISLIKED.get(), avoidTarget);
            } else {
                return false;
            }
        }
    }

    public static boolean wantsToAvoid(EntityType<?> entityType) {
        return entityType.is(COTWTags.DOG_DISLIKED);
    }

    protected static InteractionResult mobInteract(Dog dog, Player player, InteractionHand hand, Supplier<InteractionResult> animalInteract) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        Level level = dog.level;
        if (dog.isTame()) {
            if (dog.isFood(stack) && dog.getHealth() < dog.getMaxHealth()) {
                dog.usePlayerItem(player, hand, stack);
                return InteractionResult.SUCCESS;
            }

            if (!(item instanceof DyeItem dyeItem)) {
                InteractionResult animalInteractResult = animalInteract.get();
                if ((!animalInteractResult.consumesAction() || dog.isBaby()) && dog.isOwnedBy(player)) {
                    dog.setOrderedToSit(!dog.isOrderedToSit());
                    dog.setJumping(false);
                    yieldAsPet(dog);
                    return InteractionResult.SUCCESS;
                }

                return animalInteractResult;
            }

            DyeColor dyeColor = dyeItem.getDyeColor();
            if (dyeColor != dog.getCollarColor()) {
                dog.setCollarColor(dyeColor);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                return InteractionResult.SUCCESS;
            }
        } else if (dog.isInteresting(stack) && !isAngry(dog)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (dog.getRandom().nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(dog, player)) {
                dog.tame(player);
                yieldAsPet(dog);
                dog.setOrderedToSit(true);
                level.broadcastEntityEvent(dog, SUCCESSFUL_TAME_ID);
            } else {
                level.broadcastEntityEvent(dog, FAILED_TAME_ID);
            }

            return InteractionResult.SUCCESS;
        }
        return animalInteract.get();
    }

    protected static boolean isAngry(Dog dog){
        return dog.getBrain().hasMemoryValue(MemoryModuleType.ANGRY_AT);
    }

    private static void yieldAsPet(Dog dog) {
        AiHelper.stopWalking(dog);
        dog.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        dog.getBrain().eraseMemory(MemoryModuleType.AVOID_TARGET);
    }

    protected static void updateActivity(Dog dog) {
        Brain<Dog> brain = dog.getBrain();
        Activity previous = brain.getActiveNonCoreActivity().orElse((Activity)null);
        brain.setActiveActivityToFirstValid(ACTIVITIES);
        Activity current = brain.getActiveNonCoreActivity().orElse((Activity) null);
        if (previous == Activity.FIGHT && current != Activity.FIGHT) {
            brain.setMemoryWithExpiry(MemoryModuleType.HAS_HUNTING_COOLDOWN, true, TIME_BETWEEN_HUNTS.sample(dog.getRandom()));
        }
        if (previous != current) {
            getSoundForCurrentActivity(dog).ifPresent(dog::playSoundEvent);
        }

        boolean hasAttackTarget = brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
        dog.setAggressive(hasAttackTarget);
        boolean shouldSprint = hasAttackTarget || brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET) || brain.hasMemoryValue(MemoryModuleType.IS_PANICKING);
        dog.setSprinting(shouldSprint);
    }

    protected static Optional<SoundEvent> getSoundForCurrentActivity(Dog dog) {
        return dog.getBrain().getActiveNonCoreActivity().map((a) -> getSoundForActivity(dog, a));
    }

    private static SoundEvent getSoundForActivity(Dog dog, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.WOLF_GROWL;
        } else if (activity == Activity.AVOID && isNearAvoidTarget(dog)) {
            return SoundEvents.WOLF_HURT; // placeholder for YELP
        }else if (dog.getRandom().nextInt(3) == 0) {
            return dog.isTame() && dog.getHealth() < dog.getMaxHealth() * 0.5F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundEvents.WOLF_AMBIENT;
        }
    }

    private static boolean isNearAvoidTarget(Dog dog) {
        Brain<Dog> brain = dog.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)
                && brain.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan(dog, DESIRED_DISTANCE_FROM_DISLIKED);
    }

    protected static void wasHurtBy(Dog dog, LivingEntity attacker) {
        Brain<Dog> brain = dog.getBrain();
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        if (dog.isBaby()) {
            setAvoidTarget(dog, attacker);
            if (Sensor.isEntityAttackableIgnoringLineOfSight(dog, attacker)) {
                broadcastAngerTarget(dog, attacker);
            }
        } else {
            maybeRetaliate(dog, attacker);
        }
    }

    private static void setAvoidTarget(Dog dog, LivingEntity target) {
        Brain<Dog> brain = dog.getBrain();
        brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, (long)RETREAT_DURATION.sample(dog.level.random));
    }

    private static void maybeRetaliate(Dog dog, LivingEntity attacker) {
        if (!dog.getBrain().isActive(Activity.AVOID)) {
            if (Sensor.isEntityAttackableIgnoringLineOfSight(dog, attacker)) {
                if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(dog, attacker, TARGET_DISTANCE_DIFFERENCE_THRESHOLD)) {
                    if (attacker.getType() == EntityType.PLAYER && dog.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                        setAngerTargetToNearestTargetablePlayerIfFound(dog, attacker);
                        broadcastUniversalAnger(dog);
                    } else {
                        setAngerTarget(dog, attacker);
                        broadcastAngerTarget(dog, attacker);
                    }
                }
            }
        }
    }

    private static void setAngerTargetToNearestTargetablePlayerIfFound(Dog dog, LivingEntity target) {
        Optional<Player> optional = AiHelper.getNearestVisibleTargetablePlayer(dog);
        if (optional.isPresent()) {
            setAngerTarget(dog, optional.get());
        } else {
            setAngerTarget(dog, target);
        }
    }

    private static void setAngerTarget(AgeableMob dog, LivingEntity target) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(dog, target)) {
            dog.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            dog.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, target.getUUID(), ANGER_DURATION.sample(dog.getRandom()));

            if (target.getType() == EntityType.PLAYER && dog.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                dog.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, ANGER_DURATION.sample(dog.getRandom()));
            }
        }
    }

    private static void broadcastUniversalAnger(Dog dog) {
        AiHelper.getNearbyAdults(dog).forEach((d) -> AiHelper.getNearestVisibleTargetablePlayer(d).ifPresent((p) -> setAngerTarget(d, p)));
    }

    private static void broadcastAngerTarget(Dog dog, LivingEntity target) {
        AiHelper.getNearbyAdults(dog).forEach((d) -> setAngerTargetIfCloserThanCurrent(d, target));
    }

    private static void setAngerTargetIfCloserThanCurrent(AgeableMob dog, LivingEntity target) {
        Optional<LivingEntity> angerTarget = AiHelper.getAngerTarget(dog);
        LivingEntity nearestTarget = BehaviorUtils.getNearestTarget(dog, angerTarget, target);
        if (angerTarget.isEmpty() || angerTarget.get() != nearestTarget) {
            setAngerTarget(dog, nearestTarget);
        }
    }

}
