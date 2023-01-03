package com.infamous.call_of_the_wild.common.entity.dog.ai;

import com.infamous.call_of_the_wild.common.registry.COTWGameEvents;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.AngerAi;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.infamous.call_of_the_wild.common.util.HunterAi;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;
import java.util.function.Predicate;

public class SharedWolfAi {
    public static final int TOO_CLOSE_TO_LEAP = 2;
    public static final int POUNCE_DISTANCE = 4;
    public static final int CLOSE_ENOUGH_TO_INTERACT = 2;
    public static final int CLOSE_ENOUGH_TO_LOOK_TARGET = 3;
    static final float LEAP_YD = 0.4F;
    static final int INTERACTION_RANGE = 8;
    public static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    public static final UniformInt ANGER_DURATION = TimeUtil.rangeOfSeconds(20, 39); // same as Wolf's persistent anger time
    static final UniformInt AVOID_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    public static final UniformInt TIME_BETWEEN_HOWLS = TimeUtil.rangeOfSeconds(30, 120);
    static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(5, 7); // 30-120
    static final float JUMP_CHANCE_IN_WATER = 0.8F;
    static final float SPEED_MODIFIER_BREEDING = 1.0F;
    static final float SPEED_MODIFIER_CHASING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    static final float SPEED_MODIFIER_FOLLOWING_ADULT = 1.0F;
    static final float SPEED_MODIFIER_PANICKING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    static final float SPEED_MODIFIER_RETREATING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    static final float SPEED_MODIFIER_TEMPTED = 1.0F;
    static final float SPEED_MODIFIER_WALKING = 1.0F;
    static final int ATTACK_COOLDOWN_TICKS = 20;
    static final int DESIRED_DISTANCE_FROM_DISLIKED = 6;
    static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
    static final int MAX_LOOK_DIST = 8;
    static final byte SUCCESSFUL_TAME_ID = 7;
    static final byte FAILED_TAME_ID = 6;
    private static final int LLAMA_MAX_STRENGTH = 5;
    static final int CLOSE_ENOUGH_TO_FOLLOW_TARGET = 2;
    public static final int TOO_FAR_TO_SWITCH_TARGETS = 4;
    static final int TOO_FAR_FROM_FOLLOW_TARGET = 10;
    private static final int HOWL_VOLUME = 4;

    public static void initMemories(TamableAnimal wolf, RandomSource randomSource) {
        int huntCooldownInTicks = TIME_BETWEEN_HUNTS.sample(randomSource);
        HunterAi.setHuntedRecently(wolf, huntCooldownInTicks);
        int howlCooldownInTicks = TIME_BETWEEN_HOWLS.sample(randomSource);
        setHowledRecently(wolf, howlCooldownInTicks);
    }

    static boolean shouldPanic(TamableAnimal wolf) {
        return wolf.isFreezing() || wolf.isOnFire();
    }

    static boolean isNearDisliked(TamableAnimal wolf) {
        return GenericAi.isNearDisliked(wolf, DESIRED_DISTANCE_FROM_DISLIKED);
    }

    static boolean canStartAttacking(TamableAnimal wolf) {
        return !wolf.isBaby()
                && canMove(wolf)
                && !BehaviorUtils.isBreeding(wolf);
    }

    static boolean canAvoid(TamableAnimal wolf){
        return !wolf.isTame();
    }

    static boolean canFollowOwner(LivingEntity wolf) {
        return !BehaviorUtils.isBreeding(wolf);
    }

    static boolean canMakeLove(TamableAnimal wolf){
        return canMove(wolf);
    }

    static boolean canFollowNonOwner(TamableAnimal wolf) {
        return !wolf.isTame();
    }

    @SuppressWarnings("unused")
    static float getSpeedModifierTempted(LivingEntity wolf) {
        return SPEED_MODIFIER_TEMPTED;
    }

    static boolean canWander(TamableAnimal wolf){
        return canMove(wolf);
    }

    static Optional<? extends LivingEntity> findNearestValidAttackTarget(TamableAnimal wolf) {
        Brain<?> brain = wolf.getBrain();
        Optional<LivingEntity> angryAt = BehaviorUtils.getLivingEntityFromUUIDMemory(wolf, MemoryModuleType.ANGRY_AT);
        if (angryAt.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(wolf, angryAt.get())) {
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

    static boolean wantsToRetaliate(TamableAnimal wolf, LivingEntity attacker) {
        LivingEntity owner = wolf.getOwner();
        if(owner == null) return true;
        return wolf.wantsToAttack(attacker, owner);
    }

    /**
     * Called by {@link com.infamous.call_of_the_wild.common.sensor.WolfSpecificSensor#doTick(ServerLevel, Wolf)}
     * and {@link com.infamous.call_of_the_wild.common.sensor.DogSpecificSensor#doTick(ServerLevel, Dog)}
     */
    public static boolean isDisliked(TamableAnimal wolf, LivingEntity target, TagKey<EntityType<?>> disliked) {
        return target.getType().is(disliked) || target instanceof Llama llama && llama.getStrength() >= wolf.getRandom().nextInt(LLAMA_MAX_STRENGTH);
    }

    /**
     * Called by {@link com.infamous.call_of_the_wild.common.sensor.WolfSpecificSensor#doTick(ServerLevel, Wolf)}
     * and {@link com.infamous.call_of_the_wild.common.sensor.DogSpecificSensor#doTick(ServerLevel, Dog)}
     */
    public static boolean isHuntable(TamableAnimal wolf, LivingEntity livingEntity, TagKey<EntityType<?>> huntTargets, boolean requireLineOfSight) {
        return AiUtil.isHuntable(wolf, livingEntity, huntTargets, requireLineOfSight) || AiUtil.isHuntableBabyTurtle(wolf, livingEntity);
    }

    public static void setHowledRecently(LivingEntity wolf, int howlCooldownInTicks) {
        wolf.getBrain().setMemoryWithExpiry(COTWMemoryModuleTypes.HOWLED_RECENTLY.get(), true, howlCooldownInTicks);
    }

    public static boolean canBeAlertedBy(TamableAnimal wolf, LivingEntity target, Predicate<LivingEntity> isPrey){
        if (target.getType() == wolf.getType()) {
            return false;
        } else if (!(isPrey.test(target)) && !(target instanceof Monster)) {
            if (target instanceof TamableAnimal otherTamable) {
                return !otherTamable.isTame();
            } else if (!(target instanceof Player player) || !player.isSpectator() && !player.isCreative()) {
                if (wolf.isOwnedBy(target)) {
                    return false;
                } else {
                    return !target.isSleeping() && !target.isDiscrete();
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean canMove(TamableAnimal wolf) {
        return !wolf.isSleeping() && !wolf.isInSittingPose();
    }

    public static void startHunting(TamableAnimal wolf, LivingEntity target) {
        // hunt cooldown is already set for the wolf at this point
        int angerTimeInTicks = ANGER_DURATION.sample(wolf.level.random);
        AngerAi.setAngerTarget(wolf, target, angerTimeInTicks);
        AngerAi.broadcastAngerTarget(GenericAi.getNearbyAdults(wolf), target, ANGER_DURATION);
        HunterAi.broadcastHuntedRecently(TIME_BETWEEN_HUNTS, GenericAi.getNearbyVisibleAdults(wolf));
    }

    public static void clearStates(TamableAnimal wolf) {
        //wolf.setIsInterested(false);
        if(wolf.hasPose(Pose.CROUCHING)){
            wolf.setPose(Pose.STANDING);
            wolf.getBrain().eraseMemory(COTWMemoryModuleTypes.POUNCE_DELAY.get());
        }
        if(wolf.isOrderedToSit() || wolf.isInSittingPose()){
            wolf.setOrderedToSit(false);
            wolf.setInSittingPose(false);
        }
        if(wolf.isSleeping()){
            GenericAi.wakeUp(wolf);
        }
    }

    static void tellAlliesIWasAttacked(TamableAnimal wolf, LivingEntity attacker) {
        if (wolf.isBaby()) {
            GenericAi.setAvoidTarget(wolf, attacker, RETREAT_DURATION.sample(wolf.level.random));
            if (Sensor.isEntityAttackableIgnoringLineOfSight(wolf, attacker)) {
                AngerAi.broadcastAngerTarget(GenericAi.getNearbyAdults(wolf).stream()
                        .map(TamableAnimal.class::cast)
                        .filter(w -> wantsToRetaliate(w, attacker))
                        .toList(),
                        attacker,
                        ANGER_DURATION);
            }
        } else if(!wolf.getBrain().isActive(Activity.AVOID)){
            AngerAi.maybeRetaliate(wolf, GenericAi.getNearbyAdults(wolf).stream()
                    .map(TamableAnimal.class::cast)
                    .filter(w -> wantsToRetaliate(w, attacker))
                    .toList(),
                    attacker,
                    ANGER_DURATION,
                    TOO_FAR_TO_SWITCH_TARGETS);
        }
    }

    static boolean canSleep(TamableAnimal wolf) {
        return wolf.level.isDay()
                && wolf.getBrain().hasMemoryValue(COTWMemoryModuleTypes.HAS_SHELTER.get())
                && !wolf.getBrain().hasMemoryValue(COTWMemoryModuleTypes.IS_ALERT.get())
                && !wolf.isInPowderSnow;
    }

    public static void followHowl(TamableAnimal wolf, BlockPos blockPos) {
        GlobalPos howlPos = GlobalPos.of(wolf.getLevel().dimension(), blockPos);
        wolf.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockPos));
        wolf.getBrain().setMemory(COTWMemoryModuleTypes.HOWL_LOCATION.get(), howlPos);
        wolf.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    public static Optional<PositionTracker> getHowlPosition(LivingEntity wolf) {
        Brain<?> brain = wolf.getBrain();
        Optional<GlobalPos> howlLocation = getHowlLocation(wolf);
        if (howlLocation.isPresent()) {
            GlobalPos globalPos = howlLocation.get();
            if (wolf.getLevel().dimension() == globalPos.dimension()) {
                return Optional.of(new BlockPosTracker(globalPos.pos()));
            }
            brain.eraseMemory(COTWMemoryModuleTypes.HOWL_LOCATION.get());
        }
        return Optional.empty();
    }

    public static Optional<PositionTracker> getOwnerPositionTracker(LivingEntity wolf) {
        return getOwner((TamableAnimal) wolf).map((owner) -> new EntityTracker(owner, true));
    }

    public static Optional<LivingEntity> getOwner(TamableAnimal wolf) {
        return Optional.ofNullable(wolf.getOwner()).filter(le -> !le.isSpectator());
    }

    public static Optional<GlobalPos> getHowlLocation(LivingEntity wolf) {
        return wolf.getBrain().getMemory(COTWMemoryModuleTypes.HOWL_LOCATION.get());
    }

    public static void howl(LivingEntity wolf){
        wolf.playSound(SoundEvents.WOLF_HOWL, HOWL_VOLUME, wolf.getVoicePitch());
        wolf.gameEvent(COTWGameEvents.ENTITY_HOWL.get());
    }

    public static boolean hasHowledRecently(LivingEntity wolf) {
        return wolf.getBrain().hasMemoryValue(COTWMemoryModuleTypes.HOWLED_RECENTLY.get());
    }
}
