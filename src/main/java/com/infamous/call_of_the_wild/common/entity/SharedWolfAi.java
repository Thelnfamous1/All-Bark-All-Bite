package com.infamous.call_of_the_wild.common.entity;

import com.infamous.call_of_the_wild.common.entity.dog.Dog;
import com.infamous.call_of_the_wild.common.registry.ABABGameEvents;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;

public class SharedWolfAi {
    public static final int TOO_CLOSE_TO_LEAP = 2;
    public static final int POUNCE_DISTANCE = 4;
    public static final int CLOSE_ENOUGH_TO_INTERACT = 2;
    public static final int CLOSE_ENOUGH_TO_LOOK_TARGET = 3;
    public static final float LEAP_YD = 0.4F;
    public static final int INTERACTION_RANGE = 8;
    public static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    public static final UniformInt ANGER_DURATION = TimeUtil.rangeOfSeconds(20, 39); // same as Wolf's persistent anger time
    public static final UniformInt AVOID_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    public static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    public static final UniformInt TIME_BETWEEN_HOWLS = TimeUtil.rangeOfSeconds(30, 120);
    public static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(5, 7); // 30-120
    public static final float JUMP_CHANCE_IN_WATER = 0.8F;
    public static final float SPEED_MODIFIER_BREEDING = 1.0F;
    public static final float SPEED_MODIFIER_CHASING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    public static final float SPEED_MODIFIER_FOLLOWING_ADULT = 1.0F;
    public static final float SPEED_MODIFIER_PANICKING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    public static final float SPEED_MODIFIER_RETREATING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    public static final float SPEED_MODIFIER_TEMPTED = 1.0F;
    public static final float SPEED_MODIFIER_WALKING = 1.0F;
    public static final int ATTACK_COOLDOWN_TICKS = 20;
    public static final int DESIRED_DISTANCE_FROM_DISLIKED = 6;
    public static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
    public static final int MAX_LOOK_DIST = 8;
    public static final byte SUCCESSFUL_TAME_ID = 7;
    public static final byte FAILED_TAME_ID = 6;
    //public static final int LLAMA_MAX_STRENGTH = 5;
    public static final int CLOSE_ENOUGH_TO_FOLLOW_TARGET = 2;
    public static final int TOO_FAR_TO_SWITCH_TARGETS = 4;
    public static final int TOO_FAR_FROM_FOLLOW_TARGET = 10;
    private static final int HOWL_VOLUME = 4;

    public static void initMemories(TamableAnimal wolf, RandomSource randomSource) {
        int huntCooldownInTicks = TIME_BETWEEN_HUNTS.sample(randomSource);
        HunterAi.setHuntedRecently(wolf, huntCooldownInTicks);
        int howlCooldownInTicks = TIME_BETWEEN_HOWLS.sample(randomSource);
        setHowledRecently(wolf, howlCooldownInTicks);
    }

    public static boolean shouldPanic(TamableAnimal wolf) {
        return wolf.isFreezing() || wolf.isOnFire();
    }

    public static boolean isNearDisliked(TamableAnimal wolf) {
        return GenericAi.isNearDisliked(wolf, DESIRED_DISTANCE_FROM_DISLIKED);
    }

    public static boolean canStartAttacking(TamableAnimal wolf) {
        return !wolf.isBaby()
                && canMove(wolf)
                && !BehaviorUtils.isBreeding(wolf);
    }

    public static boolean canAvoid(TamableAnimal wolf){
        return !wolf.isTame();
    }

    public static boolean canFollowOwner(LivingEntity wolf) {
        return !BehaviorUtils.isBreeding(wolf);
    }

    public static boolean canMakeLove(TamableAnimal wolf){
        return canMove(wolf);
    }

    public static boolean canFollowNonOwner(TamableAnimal wolf) {
        return !wolf.isTame();
    }

    @SuppressWarnings("unused")
    public static float getSpeedModifierTempted(LivingEntity wolf) {
        return SPEED_MODIFIER_TEMPTED;
    }

    public static boolean canWander(TamableAnimal wolf){
        return canMove(wolf);
    }

    public static Optional<? extends LivingEntity> findNearestValidAttackTarget(TamableAnimal wolf) {
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

    public static boolean wantsToRetaliate(TamableAnimal wolf, LivingEntity attacker) {
        LivingEntity owner = wolf.getOwner();
        if(owner == null) return true;
        return wolf.wantsToAttack(attacker, owner);
    }

    /**
     * Called by {@link com.infamous.call_of_the_wild.common.sensor.WolfSpecificSensor#doTick(ServerLevel, Wolf)}
     * and {@link com.infamous.call_of_the_wild.common.sensor.DogSpecificSensor#doTick(ServerLevel, Dog)}
     */
    public static boolean isDisliked(LivingEntity target, TagKey<EntityType<?>> disliked) {
        return target.getType().is(disliked); //|| target instanceof Llama llama && llama.getStrength() >= wolf.getRandom().nextInt(LLAMA_MAX_STRENGTH);
    }

    /**
     * Called by {@link com.infamous.call_of_the_wild.common.sensor.WolfSpecificSensor#doTick(ServerLevel, Wolf)}
     * and {@link com.infamous.call_of_the_wild.common.sensor.DogSpecificSensor#doTick(ServerLevel, Dog)}
     */
    public static boolean isHuntable(TamableAnimal wolf, LivingEntity livingEntity, int closeEnough, TagKey<EntityType<?>> huntTargets, boolean requireLineOfSight) {
        return AiUtil.isHuntable(wolf, livingEntity, closeEnough, huntTargets, requireLineOfSight) || AiUtil.isHuntableBabyTurtle(wolf, livingEntity, closeEnough, requireLineOfSight);
    }

    public static void setHowledRecently(LivingEntity wolf, int howlCooldownInTicks) {
        wolf.getBrain().setMemoryWithExpiry(ABABMemoryModuleTypes.HOWLED_RECENTLY.get(), true, howlCooldownInTicks);
    }

    public static boolean canBeAlertedBy(TamableAnimal wolf, LivingEntity target){
        if (!(target instanceof Animal) && !(target instanceof Monster)) {
            if (!(target instanceof Player player) || !target.isSpectator() && !player.isCreative()) {
                if (wolf.isOwnedBy(target)) {
                    return false;
                } else {
                    return !target.isSleeping() && !target.isDiscrete();
                }
            } else {
                return false;
            }
        } else if (target instanceof TamableAnimal tamableAnimal) {
            LivingEntity owner = wolf.getOwner();
            return owner != null ? wolf.wantsToAttack(tamableAnimal, target) : !tamableAnimal.isTame();
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
        if(GenericAi.getAttackTarget(wolf).isEmpty()) StartAttacking.setAttackTarget(wolf, target);
        AngerAi.broadcastAngerTarget(GenericAi.getNearbyAdults(wolf), target, ANGER_DURATION);
        HunterAi.broadcastHuntedRecently(TIME_BETWEEN_HUNTS, GenericAi.getNearbyVisibleAdults(wolf));
    }

    public static void clearStates(TamableAnimal wolf) {
        //wolf.setIsInterested(false);
        if(wolf.hasPose(Pose.CROUCHING)){
            wolf.setPose(Pose.STANDING);
        }
        if(wolf.isOrderedToSit() || wolf.isInSittingPose()){
            wolf.setOrderedToSit(false);
            wolf.setInSittingPose(false);
        }
        if(wolf.isSleeping()){
            GenericAi.wakeUp(wolf);
        }
    }

    public static void reactToAttack(TamableAnimal wolf, LivingEntity attacker) {
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

    public static boolean canSleep(TamableAnimal wolf) {
        return wolf.level.isDay()
                && wolf.getBrain().hasMemoryValue(ABABMemoryModuleTypes.HAS_SHELTER.get())
                && !wolf.getBrain().hasMemoryValue(ABABMemoryModuleTypes.IS_ALERT.get())
                && !wolf.isInPowderSnow;
    }

    public static void followHowl(TamableAnimal wolf, BlockPos blockPos) {
        GlobalPos howlPos = GlobalPos.of(wolf.getLevel().dimension(), blockPos);
        wolf.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockPos));
        wolf.getBrain().setMemory(ABABMemoryModuleTypes.HOWL_LOCATION.get(), howlPos);
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
            brain.eraseMemory(ABABMemoryModuleTypes.HOWL_LOCATION.get());
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
        return wolf.getBrain().getMemory(ABABMemoryModuleTypes.HOWL_LOCATION.get());
    }

    public static void howl(LivingEntity wolf){
        wolf.playSound(SoundEvents.WOLF_HOWL, HOWL_VOLUME, wolf.getVoicePitch());
        wolf.gameEvent(ABABGameEvents.ENTITY_HOWL.get());
    }

    public static boolean hasHowledRecently(LivingEntity wolf) {
        return wolf.getBrain().hasMemoryValue(ABABMemoryModuleTypes.HOWLED_RECENTLY.get());
    }
}
