package com.infamous.call_of_the_wild.common.entity;

import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.ai.AngerAi;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import com.infamous.call_of_the_wild.common.ai.HunterAi;
import com.infamous.call_of_the_wild.common.behavior.MoveToNonSkySeeingSpot;
import com.infamous.call_of_the_wild.common.entity.dog.DogGoalPackages;
import com.infamous.call_of_the_wild.common.registry.ABABGameEvents;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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
    public static final int CLOSE_ENOUGH_TO_OWNER = 2;
    public static final int TOO_FAR_TO_SWITCH_TARGETS = 4;
    public static final int TOO_FAR_FROM_WALK_TARGET = 10;
    public static final int MAX_ALERTABLE_XZ = 12;
    public static final int MAX_ALERTABLE_Y = 6;
    public static final float SPEED_MODIFIER_FETCHING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    private static final int HOWL_VOLUME = 4;
    public static final int EAT_DURATION = 60;
    public static final int MIN_TICKS_BEFORE_EAT = 600;

    public static void initMemories(TamableAnimal wolf, RandomSource randomSource) {
        int huntCooldownInTicks = TIME_BETWEEN_HUNTS.sample(randomSource);
        HunterAi.setHuntedRecently(wolf, huntCooldownInTicks);
        int howlCooldownInTicks = TIME_BETWEEN_HOWLS.sample(randomSource);
        setHowledRecently(wolf, howlCooldownInTicks);
    }

    public static boolean shouldPanic(TamableAnimal wolf) {
        return wolf.isFreezing() || wolf.isOnFire();
    }

    public static boolean canStartAttacking(TamableAnimal wolf) {
        return !wolf.isBaby()
                && canMove(wolf)
                && !BehaviorUtils.isBreeding(wolf);
    }

    public static boolean canAvoid(TamableAnimal wolf){
        return !wolf.isTame();
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

    public static boolean isDisliked(LivingEntity target, TagKey<EntityType<?>> disliked) {
        return target.getType().is(disliked); //|| target instanceof Llama llama && llama.getStrength() >= wolf.getRandom().nextInt(LLAMA_MAX_STRENGTH);
    }

    public static void setHowledRecently(LivingEntity wolf, int howlCooldownInTicks) {
        wolf.getBrain().setMemoryWithExpiry(ABABMemoryModuleTypes.HOWLED_RECENTLY.get(), true, howlCooldownInTicks);
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

    public static void clearStates(TamableAnimal tamableAnimal) {
        if(tamableAnimal.hasPose(Pose.CROUCHING)){
            tamableAnimal.setPose(Pose.STANDING);
        }
        if(tamableAnimal.isInSittingPose()){
            tamableAnimal.setInSittingPose(false);
        }
        if(tamableAnimal.isSleeping()){
            GenericAi.wakeUp(tamableAnimal);
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
                && hasShelter(wolf)
                && !alertable(wolf)
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean alertable(TamableAnimal wolf) {
        List<LivingEntity> livingEntities = GenericAi.getNearestLivingEntities(wolf);
        for(LivingEntity livingEntity : livingEntities){
            if(livingEntity.closerThan(wolf, MAX_ALERTABLE_XZ, MAX_ALERTABLE_Y)
                    && Sensor.isEntityAttackableIgnoringLineOfSight(wolf, livingEntity)
                    && canBeAlertedBy(wolf, livingEntity)){
                return true;
            }
        }
        return false;
    }

    public static boolean canBeAlertedBy(TamableAnimal wolf, LivingEntity target){
        if (AiUtil.canBeConsideredAnAlly(wolf, target)) {
            return false;
        } else {
            if (EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
                if (wolf.isOwnedBy(target)) {
                    return false;
                } else {
                    return !target.isSleeping() && !target.isDiscrete();
                }
            } else {
                return false;
            }
        }
    }

    public static boolean hasShelter(TamableAnimal wolf) {
        BlockPos topOfBodyPos = new BlockPos(wolf.getX(), wolf.getBoundingBox().maxY, wolf.getZ());
        return MoveToNonSkySeeingSpot.hasBlocksAbove(wolf.level, wolf, topOfBodyPos);
    }

    public static boolean canDefendOwner(TamableAnimal tamableAnimal){
        return tamableAnimal.isTame() && !tamableAnimal.isOrderedToSit();
    }

    public static boolean wantsToAttack(TamableAnimal tamableAnimal, LivingEntity target, LivingEntity owner){
        return tamableAnimal.wantsToAttack(target, owner);
    }

    public static void setAteRecently(Animal animal){
        animal.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, MIN_TICKS_BEFORE_EAT - EAT_DURATION);
    }

    public static boolean canSprintCore(TamableAnimal wolf){
        return canMove(wolf) && GenericAi.isPanicking(wolf);
    }

    public static void stopHoldingItemInMouth(LivingEntity livingEntity) {
        spitOutItem(livingEntity, livingEntity.getMainHandItem());
        livingEntity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        AiUtil.setItemPickupCooldown(livingEntity, DogGoalPackages.ITEM_PICKUP_COOLDOWN);
    }

    protected static void spitOutItem(LivingEntity livingEntity, ItemStack itemStack) {
        if (!itemStack.isEmpty() && !livingEntity.level.isClientSide) {
            ItemEntity itemEntity = new ItemEntity(livingEntity.level, livingEntity.getX() + livingEntity.getLookAngle().x, livingEntity.getY() + 1.0D, livingEntity.getZ() + livingEntity.getLookAngle().z, itemStack);
            itemEntity.setPickUpDelay(40);
            itemEntity.setThrower(livingEntity.getUUID());
            livingEntity.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
            livingEntity.level.addFreshEntity(itemEntity);
        }
    }

    public static void holdInMouth(Mob mob, ItemStack stack) {
        if (!mob.getMainHandItem().isEmpty()) {
            stopHoldingItemInMouth(mob);
        }

        mob.setItemSlot(EquipmentSlot.MAINHAND, stack);
        mob.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        mob.setPersistenceRequired();
    }

    public static ItemStack pickUpAndHoldItem(Mob mob, ItemEntity itemEntity) {
        mob.take(itemEntity, 1);
        ItemStack singleton = MiscUtil.removeOneItemFromItemEntity(itemEntity);
        holdInMouth(mob, singleton);
        AiUtil.setItemPickupCooldown(mob, DogGoalPackages.ITEM_PICKUP_COOLDOWN);
        return singleton;
    }

    public static boolean isNotHoldingItem(LivingEntity livingEntity) {
        return livingEntity.getMainHandItem().isEmpty();
    }

    public static RunIf<TamableAnimal> createGoToWantedItem(Predicate<TamableAnimal> canWander, boolean overrideWalkTarget) {
        return new RunIf<>(canWander, new GoToWantedItem<>(SharedWolfAi::isNotHoldingItem, SharedWolfAi.SPEED_MODIFIER_FETCHING, overrideWalkTarget, DogGoalPackages.MAX_FETCH_DISTANCE));
    }

    public static boolean isAbleToPickUp(Mob mob, ItemStack stack) {
        if (AiUtil.hasAnyMemory(mob,
                MemoryModuleType.ATTACK_TARGET,
                MemoryModuleType.AVOID_TARGET,
                MemoryModuleType.IS_PANICKING,
                MemoryModuleType.BREED_TARGET)) {
            return false;
        } else {
            return mob.canHoldItem(stack);
        }
    }
}
