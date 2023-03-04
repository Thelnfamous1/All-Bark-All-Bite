package com.infamous.all_bark_all_bite.common.entity;

import com.infamous.all_bark_all_bite.common.behavior.pet.FollowOwner;
import com.infamous.all_bark_all_bite.common.behavior.sleep.MoveToNonSkySeeingSpot;
import com.infamous.all_bark_all_bite.common.entity.wolf.WolfHooks;
import com.infamous.all_bark_all_bite.common.registry.ABABGameEvents;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.CompatUtil;
import com.infamous.all_bark_all_bite.common.util.DICompat;
import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import com.infamous.all_bark_all_bite.common.util.PetUtil;
import com.infamous.all_bark_all_bite.common.util.ai.*;
import com.infamous.all_bark_all_bite.config.ABABConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class SharedWolfAi {
    public static final float LEAP_YD = 0.4F;
    public static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    public static final UniformInt ANGER_DURATION = TimeUtil.rangeOfSeconds(20, 39); // same as Wolf's persistent anger time
    public static final UniformInt AVOID_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    public static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
    public static final UniformInt TIME_BETWEEN_HOWLS = TimeUtil.rangeOfSeconds(30, 120);
    public static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
    public static final UniformInt LEAP_COOLDOWN = TimeUtil.rangeOfSeconds(4, 6);
    public static final UniformInt PLAY_POUNCE_COOLDOWN = TimeUtil.rangeOfSeconds(30, 60);
    public static final UniformInt HUNT_POUNCE_COOLDOWN = TimeUtil.rangeOfSeconds(5, 7);
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
    public static final int TOO_FAR_FROM_OWNER = 10;
    public static final float SPEED_MODIFIER_FETCHING = 1.0F; // Dog will sprint with 30% extra speed, meaning final speed is effectively ~1.3F
    public static final int POUNCE_HEIGHT = 3;
    public static final int INTERACTION_RANGE = 8;
    public static final int ITEM_PICKUP_COOLDOWN = 60;
    public static final int MAX_FETCH_DISTANCE = 16;
    public static final int DISABLE_FETCH_TIME = 200;
    public static final int MAX_TIME_TO_REACH_ITEM = 200;
    public static final float FALL_REDUCTION = 5.0F;
    public static final int TOO_CLOSE_TO_LEAP = 2;
    public static final int TOO_FAR_TO_LEAP = 4;
    public static final int POUNCE_DISTANCE = 6;
    public static final int CLOSE_ENOUGH_TO_INTERACT = 2;
    public static final int CLOSE_ENOUGH_TO_LOOK_TARGET = 3;
    public static final int BABY_POUNCE_DISTANCE = 4;
    public static final int BABY_POUNCE_HEIGHT = 2;
    public static final float LONG_JUMPING_SCALE = 0.7F;
    private static final int HOWL_VOLUME = 4;
    public static final int EAT_DURATION = MiscUtil.seconds(3);
    public static final int EAT_COOLDOWN = MiscUtil.seconds(30);
    public static final long DIG_DURATION = 100L;
    public static final int DEFAULT_LISTENER_RANGE = 64;
    public static final UniformInt TAG_DURATION = TimeUtil.rangeOfSeconds(10, 30);

    public static void initMemories(TamableAnimal wolf, RandomSource randomSource) {
        int huntCooldownInTicks = TIME_BETWEEN_HUNTS.sample(randomSource);
        HunterAi.setHuntedRecently(wolf, huntCooldownInTicks);
    }

    public static boolean shouldPanic(TamableAnimal wolf) {
        return wolf.isFreezing() || wolf.isOnFire();
    }

    public static boolean canStartAttacking(TamableAnimal wolf) {
        return !wolf.isBaby()
                && canMove(wolf)
                && !BehaviorUtils.isBreeding(wolf);
    }

    public static float getSpeedModifierTempted(LivingEntity ignoredWolf) {
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

    public static void clearStates(TamableAnimal tamableAnimal, boolean resetSit) {
        if(tamableAnimal.hasPose(Pose.CROUCHING)){
            tamableAnimal.setPose(Pose.STANDING);
        }
        if(tamableAnimal.isInSittingPose() && resetSit){
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
                        .filter(w -> wantsToRetaliate(w, attacker))
                        .toList(),
                        attacker,
                        ANGER_DURATION);
            }
        } else if(!wolf.getBrain().isActive(Activity.AVOID)){
            AngerAi.maybeRetaliate(wolf, GenericAi.getNearbyAdults(wolf).stream()
                    .filter(w -> wantsToRetaliate(w, attacker))
                    .toList(),
                    attacker,
                    ANGER_DURATION,
                    TOO_FAR_TO_SWITCH_TARGETS);
        }
    }

    public static boolean canSleep(TamableAnimal wolf, boolean nocturnal) {
        return nocturnal ? wolf.level.isDay() : wolf.level.isNight()
                && hasShelter(wolf)
                && !isAlert(wolf)
                && !wolf.isInPowderSnow;
    }

    public static boolean isAlert(LivingEntity wolf) {
        return wolf.getBrain().hasMemoryValue(ABABMemoryModuleTypes.IS_ALERT.get());
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
    public static boolean alertable(TamableAnimal wolf, TagKey<EntityType<?>> huntTargets, TagKey<EntityType<?>> alwaysHostiles, TagKey<EntityType<?>> disliked) {
        List<LivingEntity> nle = GenericAi.getNearestLivingEntities(wolf);
        for(LivingEntity entity : nle){
            if(entity.closerThan(wolf, ABABConfig.alertableMaxXZDistance.get(), ABABConfig.alertableMaxYDistance.get())
                    && AiUtil.isEntityTargetableIgnoringLineOfSight(wolf, entity)
                    && canBeAlertedBy(wolf, entity, huntTargets, alwaysHostiles, disliked)){
                return true;
            }
        }
        return false;
    }

    private static boolean canBeAlertedBy(TamableAnimal wolf, LivingEntity target, TagKey<EntityType<?>> huntTargets, TagKey<EntityType<?>> alwaysHostiles, TagKey<EntityType<?>> disliked){
        if (AiUtil.isSameTypeAndFriendly(wolf, target)) {
            return false;
        } else {
            if(CompatUtil.isDILoaded()){
                if(DICompat.isTamed(target)) return false;
            } else {
                Optional<Boolean> tame = PetUtil.isTame(target);
                if (tame.isPresent()) {
                    return !tame.get();
                } else if (EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
                    if (wolf.isOwnedBy(target)) {
                        return false;
                    } else {
                        return !target.isSleeping() && !target.isDiscrete();
                    }
                }
            }
            return target.getType().is(huntTargets) || target.getType().is(alwaysHostiles) || target.getType().is(disliked);
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
        animal.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, EAT_COOLDOWN - EAT_DURATION);
    }

    public static void stopHoldingItemInMouth(LivingEntity livingEntity) {
        if(!livingEntity.getMainHandItem().isEmpty()){
            spitOutItem(livingEntity, livingEntity.getMainHandItem());
            livingEntity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            AiUtil.setItemPickupCooldown(livingEntity, ITEM_PICKUP_COOLDOWN);
        }
    }

    private static void spitOutItem(LivingEntity livingEntity, ItemStack itemStack) {
        if (!itemStack.isEmpty() && !livingEntity.level.isClientSide) {
            ItemEntity itemEntity = new ItemEntity(livingEntity.level, livingEntity.getX() + livingEntity.getLookAngle().x, livingEntity.getY() + 1.0D, livingEntity.getZ() + livingEntity.getLookAngle().z, itemStack);
            itemEntity.setDefaultPickUpDelay();
            itemEntity.setThrower(livingEntity.getUUID());
            AiUtil.playSoundEvent(livingEntity, SoundEvents.FOX_SPIT);
            livingEntity.level.addFreshEntity(itemEntity);
        }
    }

    public static void holdInMouth(Mob mob, ItemStack stack) {
        stopHoldingItemInMouth(mob);
        mob.setItemSlot(EquipmentSlot.MAINHAND, stack);
        mob.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        mob.setPersistenceRequired();
    }

    public static void pickUpAndHoldItem(Mob mob, ItemEntity itemEntity) {
        mob.take(itemEntity, 1);
        ItemStack singleton = MiscUtil.removeOneItemFromItemEntity(itemEntity);
        holdInMouth(mob, singleton);
        AiUtil.setItemPickupCooldown(mob, ITEM_PICKUP_COOLDOWN);
    }

    public static boolean isNotHoldingItem(LivingEntity livingEntity) {
        return livingEntity.getMainHandItem().isEmpty();
    }

    public static <E extends TamableAnimal> GoToWantedItem<E> createGoToWantedItem(boolean overrideWalkTarget) {
        return new GoToWantedItem<>(SharedWolfAi::isNotHoldingItem, SharedWolfAi.SPEED_MODIFIER_FETCHING, overrideWalkTarget, MAX_FETCH_DISTANCE);
    }

    public static boolean isAbleToPickUp(Mob mob, ItemStack stack) {
        if (!mob.getBrain().isActive(Activity.IDLE)) {
            return false;
        } else {
            if(mob.getType() == EntityType.WOLF && CompatUtil.isRWLoaded()){
                return WolfHooks.canWolfHoldItem(stack, (Animal) mob);
            } else {
                return mob.canHoldItem(stack);
            }
        }
    }

    public static boolean wantsToFindShelter(LivingEntity livingEntity, boolean nocturnal){
        return livingEntity.level.isThundering() || nocturnal ? livingEntity.level.isDay() : livingEntity.level.isNight();
    }

    public static boolean isInDayTime(LivingEntity livingEntity){
        return livingEntity.level.isDay();
    }

    public static boolean isInNightTime(LivingEntity livingEntity){
        return livingEntity.level.isNight();
    }

    public static boolean wantsToWakeUp(TamableAnimal wolf){
        return !wolf.getBrain().isActive(Activity.REST)
                || GenericAi.getAttackTarget(wolf).isPresent()
                || wolf.level.isThundering()
                || wolf.isInWater();
    }

    public static boolean isNearDisliked(TamableAnimal wolf, MemoryModuleType<? extends LivingEntity> dislikedMemory){
        return GenericAi.isNearTarget(wolf, DESIRED_DISTANCE_FROM_DISLIKED, dislikedMemory);
    }

    public static void handleSleeping(TamableAnimal wolf) {
        if (wolf.isSleeping()) {
            if(wolf.isInSittingPose()){
                wolf.setInSittingPose(false);
            }
            wolf.setJumping(false);
            wolf.xxa = 0.0F;
            wolf.zza = 0.0F;
        }
    }

    public static FollowOwner<TamableAnimal> createFollowOwner(float speedModifier) {
        return new FollowOwner<>(SharedWolfAi::dontFollowIf, AiUtil::getOwner, speedModifier, CLOSE_ENOUGH_TO_OWNER, SharedWolfAi::getFollowOwnerTriggerDistance);
    }

    private static int getFollowOwnerTriggerDistance(TamableAnimal tamableAnimal){
        return isFetching(tamableAnimal) ?
                CLOSE_ENOUGH_TO_OWNER : CommandAi.getFollowTriggerDistance(tamableAnimal);
    }

    private static boolean isFetching(TamableAnimal tamableAnimal) {
        return tamableAnimal.getBrain().hasMemoryValue(ABABMemoryModuleTypes.FETCHING_ITEM.get());
    }

    private static boolean dontFollowIf(TamableAnimal dog){
        return dog.isOrderedToSit();
    }

    public static void tame(TamableAnimal wolf, Player player) {
        wolf.tame(player);
        wolf.setOrderedToSit(true);
        CommandAi.yieldAsPet(wolf);
    }

    public static void manualCommand(TamableAnimal wolf, Player player) {
        boolean orderedToSit = wolf.isOrderedToSit();
        boolean isFollowingOwner = CommandAi.isFollowing(wolf);
        if(orderedToSit && isFollowingOwner){
            CommandAi.stopFollowing(wolf);
            isFollowingOwner = false;
        }
        CommandAi.resetFollowTriggerDistance(wolf);
        if(!orderedToSit && !isFollowingOwner){ // If wandering, set sitting
            if(CompatUtil.isDILoaded()){
                DICompat.setDICommand(wolf, player, DICompat.DI_STAY_COMMAND);
            }
            wolf.setOrderedToSit(true);
            CommandAi.stopFollowing(wolf);
        } else if(orderedToSit){ // If sitting, set following
            if(CompatUtil.isDILoaded()){
                DICompat.setDICommand(wolf, player, DICompat.DI_FOLLOW_COMMAND);
            }
            wolf.setOrderedToSit(false);
            CommandAi.setFollowing(wolf);
        } else { // If following/heeling, set wandering
            if(CompatUtil.isDILoaded()){
                DICompat.setDICommand(wolf, player, DICompat.DI_WANDER_COMMAND);
            }
            CommandAi.stopFollowing(wolf);
        }
        clearStates(wolf, false);
        CommandAi.yieldAsPet(wolf);
        stopHoldingItemInMouth(wolf);
    }

    public static boolean wantsToAttack(TamableAnimal tamable, LivingEntity target){
        if(!tamable.isTame()){
            return true;
        } else{
            LivingEntity owner = tamable.getOwner();
            if(owner != null){
                return tamable.wantsToAttack(target, owner);
            }
        }
        return true;
    }

    public static float maybeReduceDamage(float amount, DamageSource source) {
        Entity sourceEntity = source.getEntity();
        // for some reason, vanilla Wolves take reduced damage from non-players and non-arrows
        if (sourceEntity != null && !(sourceEntity instanceof Player) && !(sourceEntity instanceof AbstractArrow)) {
            amount = (amount + 1.0F) / 2.0F;
        }
        return amount;
    }

}
