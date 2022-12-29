package com.infamous.call_of_the_wild.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infamous.call_of_the_wild.common.COTWTags;
import com.infamous.call_of_the_wild.common.registry.COTWActivities;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class DogAi {

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
        brain.addActivityWithConditions(Activity.CORE,
                DogGoalPackages.getCorePackage(),
                ImmutableSet.of());
    }

    private static void initFightActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.FIGHT,
                DogGoalPackages.getFightPackage(),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                ),
                ImmutableSet.of(MemoryModuleType.ATTACK_TARGET));
    }

    private static void initRetreatActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.AVOID,
                DogGoalPackages.getAvoidPackage(),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.AVOID_TARGET, MemoryStatus.VALUE_PRESENT)
                ),
                ImmutableSet.of(MemoryModuleType.AVOID_TARGET));
    }

    private static void initDiggingActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.DIG,
                DogGoalPackages.getDigPackage(),
                ImmutableSet.of(
                        Pair.of(COTWMemoryModuleTypes.DIG_LOCATION.get(), MemoryStatus.VALUE_PRESENT)
                ),
                ImmutableSet.of(COTWMemoryModuleTypes.DIG_LOCATION.get()));
    }

    private static void initFetchActivity(Brain<Dog> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(COTWActivities.FETCH.get(),
                DogGoalPackages.getFetchPackage(),
                ImmutableSet.of(
                        Pair.of(COTWMemoryModuleTypes.FETCHING_ITEM.get(), MemoryStatus.VALUE_PRESENT)
                ),
                ImmutableSet.of(COTWMemoryModuleTypes.FETCHING_ITEM.get()));
    }

    private static void initIdleActivity(Brain<Dog> brain) {
        brain.addActivity(Activity.IDLE, DogGoalPackages.getIdlePackage());
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
                if(DogGoalPackages.canBury(stack) && !DogGoalPackages.hasDigLocation(dog) && !hasDigCooldown(dog)){
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
                level.broadcastEntityEvent(dog, SharedWolfAi.SUCCESSFUL_TAME_ID);
            } else {
                level.broadcastEntityEvent(dog, SharedWolfAi.FAILED_TAME_ID);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
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

        DogGoalPackages.setItemPickupCooldown(dog);

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
            DogGoalPackages.stopHoldingItemInMouth(dog);
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
        } else if (activity == Activity.AVOID && GenericAi.isNearAvoidTarget(dog, SharedWolfAi.DESIRED_DISTANCE_FROM_DISLIKED)) {
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
        } else if (DogGoalPackages.canFetch(stack)) {
            return DogGoalPackages.isNotHoldingItem(dog) && dog.isTame();
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
