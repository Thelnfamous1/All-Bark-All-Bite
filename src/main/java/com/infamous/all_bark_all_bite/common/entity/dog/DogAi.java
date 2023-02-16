package com.infamous.all_bark_all_bite.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.ai.CommandAi;
import com.infamous.all_bark_all_bite.common.ai.DigAi;
import com.infamous.all_bark_all_bite.common.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABSensorTypes;
import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Collection;
import java.util.Optional;

public class DogAi {

    public static final Collection<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATE_RECENTLY,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.AVOID_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.DIG_COOLDOWN,
            ABABMemoryModuleTypes.DIG_LOCATION.get(),
            ABABMemoryModuleTypes.DISABLE_WALK_TO_FETCH_ITEM.get(),
            ABABMemoryModuleTypes.DOG_VIBRATION_LISTENER.get(),
            ABABMemoryModuleTypes.FETCHING_DISABLED.get(),
            ABABMemoryModuleTypes.FETCHING_ITEM.get(),
            //MemoryModuleType.HAS_HUNTING_COOLDOWN,
            MemoryModuleType.HUNTED_RECENTLY,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.INTERACTION_TARGET,
            ABABMemoryModuleTypes.IS_LEVEL_NIGHT.get(),
            ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get(),
            ABABMemoryModuleTypes.IS_ORDERED_TO_HEEL.get(),
            ABABMemoryModuleTypes.IS_ORDERED_TO_SIT.get(),
            MemoryModuleType.IS_PANICKING,
            ABABMemoryModuleTypes.IS_SHELTERED.get(),
            MemoryModuleType.IS_TEMPTED,
            MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
            MemoryModuleType.LAST_SLEPT,
            MemoryModuleType.LAST_WOKEN,
            MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
            MemoryModuleType.LONG_JUMP_MID_JUMP,
            MemoryModuleType.LOOK_TARGET,
            ABABMemoryModuleTypes.NEAREST_ADULTS.get(),
            ABABMemoryModuleTypes.NEAREST_BABIES.get(),
            ABABMemoryModuleTypes.NEAREST_ALLIES.get(),
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            ABABMemoryModuleTypes.NEAREST_VISIBLE_ADULTS.get(),
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            ABABMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(),
            ABABMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(),
            ABABMemoryModuleTypes.NEAREST_VISIBLE_ALLIES.get(),
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM,
            MemoryModuleType.PATH,
            ABABMemoryModuleTypes.POUNCE_COOLDOWN_TICKS.get(),
            ABABMemoryModuleTypes.POUNCE_TARGET.get(),
            ABABMemoryModuleTypes.STALK_TARGET.get(),
            MemoryModuleType.TEMPTING_PLAYER,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
            ABABMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get(),
            MemoryModuleType.UNIVERSAL_ANGER,
            MemoryModuleType.WALK_TARGET
    );
    public static final Collection<? extends SensorType<? extends Sensor<? super Dog>>> SENSOR_TYPES = ImmutableList.of(
            ABABSensorTypes.ANIMAL_TEMPTATIONS.get(),
            SensorType.HURT_BY,

            // dependent on NEAREST_VISIBLE_LIVING_ENTITIES
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_ADULT,
            ABABSensorTypes.NEAREST_ALLIES.get(),
            ABABSensorTypes.DOG_SPECIFIC_SENSOR.get(),
            ABABSensorTypes.DOG_VIBRATION_SENSOR.get(),

            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_PLAYERS
    );
    public static final int DIG_MAX_XZ_DISTANCE = 10;
    public static final int DIG_MAX_Y_DISTANCE = 7;

    /**
     * Called by {@link Dog#mobInteract(Player, InteractionHand)}
     */
    public static Optional<InteractionResult> mobInteract(Dog dog, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        Level level = dog.level;

        if(dog.isTame()){
            if (!(item instanceof DyeItem dyeItem)) {
                if(canBury(stack) && !AiUtil.hasAnyMemory(dog, ABABMemoryModuleTypes.DIG_LOCATION.get(), MemoryModuleType.DIG_COOLDOWN)){
                    Optional<BlockPos> digLocation = DigAi.generateDigLocation(dog, DIG_MAX_XZ_DISTANCE, DIG_MAX_Y_DISTANCE, bp -> level.getBlockState(bp.below()).is(ABABTags.DOG_CAN_DIG));
                    if(digLocation.isPresent()){
                        CommandAi.yieldAsPet(dog);
                        DigAi.setDigLocation(dog, digLocation.get());
                        ItemStack singleton = stack.split(1);
                        SharedWolfAi.holdInMouth(dog, singleton);
                        return Optional.of(InteractionResult.CONSUME);
                    } else{
                        return Optional.of(InteractionResult.PASS);
                    }
                }

                if(dog.isFood(stack) && AiUtil.isInjured(dog)){
                    dog.usePlayerItem(player, hand, stack);
                    return Optional.of(InteractionResult.CONSUME);
                }

                return Optional.empty();
            } else{
                DyeColor dyecolor = dyeItem.getDyeColor();
                if (dyecolor != dog.getCollarColor()) {
                    dog.setCollarColor(dyecolor);
                    dog.usePlayerItem(player, hand, stack);

                    return Optional.of(InteractionResult.CONSUME);
                }
            }
        } else if(dog.isFood(stack) && !dog.isAggressive()){
            dog.usePlayerItem(player, hand, stack);
            if (MiscUtil.oneInChance(dog.getRandom(), 3) && !ForgeEventFactory.onAnimalTame(dog, player)) {
                SharedWolfAi.tame(dog, player);
                level.broadcastEntityEvent(dog, SharedWolfAi.SUCCESSFUL_TAME_ID);
            } else {
                level.broadcastEntityEvent(dog, SharedWolfAi.FAILED_TAME_ID);
            }
            return Optional.of(InteractionResult.CONSUME);
        }
        return Optional.of(InteractionResult.PASS);
    }

    public static Optional<SoundEvent> getSoundForCurrentActivity(Dog dog) {
        return dog.getBrain().getActiveNonCoreActivity().map((a) -> getSoundForActivity(dog, a));
    }

    private static SoundEvent getSoundForActivity(Dog dog, Activity activity) {
        if (activity == Activity.FIGHT) {
            return SoundEvents.WOLF_GROWL;
        } else if (activity == Activity.AVOID && GenericAi.isNearAvoidTarget(dog, SharedWolfAi.DESIRED_DISTANCE_FROM_DISLIKED)) {
            return SoundEvents.WOLF_HURT;
        } else if (activity == Activity.REST) {
            return SoundEvents.FOX_SLEEP;
        } else if (MiscUtil.oneInChance(dog.getRandom(), 3)) {
            return dog.isTame() && dog.getHealth() < dog.getMaxHealth() * 0.5F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundEvents.WOLF_AMBIENT;
        }
    }

    /**
     * Called by {@link Dog#wantsToPickUp(ItemStack)}
     */
    public static boolean wantsToPickup(Mob dog, ItemStack stack) {
        return DigAi.getDigLocation(dog).isEmpty() && SharedWolfAi.isAbleToPickUp(dog, stack);
    }

    /**
     * Called by {@link Dog#pickUpItem(ItemEntity)}
     */
    public static void pickUpItem(Dog dog, ItemEntity itemEntity) {
        SharedWolfAi.pickUpAndHoldItem(dog, itemEntity);
        dog.getBrain().eraseMemory(ABABMemoryModuleTypes.TIME_TRYING_TO_REACH_FETCH_ITEM.get());
    }

    protected static boolean canBury(ItemStack stack) {
        return stack.is(ABABTags.DOG_BURIES);
    }

    public static boolean isDisliked(LivingEntity livingEntity) {
        return SharedWolfAi.isDisliked(livingEntity, ABABTags.DOG_DISLIKED);
    }

    public static boolean isInteresting(Dog dog, ItemStack stack) {
        return canBury(stack) || dog.isFood(stack);
    }
}
