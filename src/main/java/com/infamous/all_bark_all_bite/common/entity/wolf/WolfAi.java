package com.infamous.all_bark_all_bite.common.entity.wolf;

import com.google.common.collect.ImmutableList;
import com.infamous.all_bark_all_bite.common.ABABTags;
import com.infamous.all_bark_all_bite.common.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.ai.BrainUtil;
import com.infamous.all_bark_all_bite.common.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.entity.AnimalAccessor;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.registry.ABABActivities;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.registry.ABABSensorTypes;
import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class WolfAi {
    public static final Collection<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATE_RECENTLY,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.AVOID_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            ABABMemoryModuleTypes.FOLLOWERS.get(),
            ABABMemoryModuleTypes.HOWL_LOCATION.get(),
            ABABMemoryModuleTypes.HOWLED_RECENTLY.get(),
            MemoryModuleType.HUNTED_RECENTLY,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS,
            ABABMemoryModuleTypes.IS_LEVEL_DAY.get(),
            ABABMemoryModuleTypes.IS_ORDERED_TO_FOLLOW.get(),
            ABABMemoryModuleTypes.IS_ORDERED_TO_HEEL.get(),
            ABABMemoryModuleTypes.IS_ORDERED_TO_SIT.get(),
            MemoryModuleType.IS_PANICKING,
            ABABMemoryModuleTypes.IS_SHELTERED.get(),
            MemoryModuleType.LAST_SLEPT,
            MemoryModuleType.LAST_WOKEN,
            ABABMemoryModuleTypes.LEADER.get(),
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
            MemoryModuleType.UNIVERSAL_ANGER,
            MemoryModuleType.WALK_TARGET,
            ABABMemoryModuleTypes.WOLF_VIBRATION_LISTENER.get()
    );
    public static final Collection<? extends SensorType<? extends Sensor<? super Wolf>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.HURT_BY,

            // dependent on NEAREST_VISIBLE_LIVING_ENTITIES
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_ADULT,
            ABABSensorTypes.NEAREST_ALLIES.get(),

            SensorType.NEAREST_ITEMS,
            SensorType.NEAREST_PLAYERS,
            ABABSensorTypes.WOLF_SPECIFIC_SENSOR.get(),
            ABABSensorTypes.WOLF_VIBRATION_SENSOR.get()
    );
    public static final float WOLF_SIZE_SCALE = 1.25F;
    public static final float WOLF_SIZE_LONG_JUMPING_SCALE = 0.7F;

    public static Optional<SoundEvent> getSoundForCurrentActivity(Wolf wolf) {
        return wolf.getBrain().getActiveNonCoreActivity().map((a) -> getSoundForActivity(wolf, a));
    }

    @Nullable
    private static SoundEvent getSoundForActivity(Wolf wolf, Activity activity) {
        if(activity == Activity.PANIC){
            return SoundEvents.WOLF_HURT;
        } else if(activity == ABABActivities.STALK.get() || activity == ABABActivities.POUNCE.get()){
            return null;
        } else if (activity == Activity.FIGHT) {
            return SoundEvents.WOLF_GROWL;
        } else if (activity == Activity.AVOID) {
            return GenericAi.isNearAvoidTarget(wolf, SharedWolfAi.DESIRED_DISTANCE_FROM_DISLIKED) ? SoundEvents.WOLF_HURT : null;
        } else if (activity == Activity.REST) {
            return SoundEvents.FOX_SLEEP;
        } else if (MiscUtil.oneInChance(wolf.getRandom(), 3)) {
            return wolf.isTame() && wolf.getHealth() < wolf.getMaxHealth() * 0.5F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundEvents.WOLF_AMBIENT;
        }
    }

    /**
     * Called by {@link com.infamous.all_bark_all_bite.common.ForgeEventHandler#onLivingUpdate(LivingEvent.LivingTickEvent)}
     */
    public static void updateAi(ServerLevel serverLevel, Wolf wolf) {
        serverLevel.getProfiler().push("wolfBrain");
        BrainUtil.getTypedBrain(wolf).tick(serverLevel, wolf);
        serverLevel.getProfiler().pop();
    }

    public static boolean isDisliked(Wolf wolf, LivingEntity livingEntity) {
        return SharedWolfAi.isDisliked(livingEntity, ABABTags.WOLF_DISLIKED)
                || isDislikedPlayer(wolf, livingEntity);
    }

    public static boolean isDislikedPlayer(Wolf wolf, LivingEntity livingEntity) {
        return livingEntity instanceof Player player
                && !wolf.isOwnedBy(player)
                && !player.isDiscrete()
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player);
    }

    public static void initMemories(Wolf wolf, RandomSource random) {
        SharedWolfAi.initMemories(wolf, random);
    }

    public static InteractionResult mobInteract(Wolf wolf, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        Item item = itemInHand.getItem();
        if (wolf.level.isClientSide) {
            if (wolf.isTame() && wolf.isOwnedBy(player)) {
                return InteractionResult.SUCCESS;
            } else {
                return !wolf.isFood(itemInHand) || !AiUtil.isInjured(wolf) && wolf.isTame() ? InteractionResult.PASS : InteractionResult.SUCCESS;
            }
        } else {
            if (wolf.isTame() && wolf.isOwnedBy(player)) {
                if (wolf.isFood(itemInHand) && AiUtil.isInjured(wolf)) {
                    AiUtil.animalEat(wolf, itemInHand);
                    AnimalAccessor.cast(wolf).takeItemFromPlayer(player, hand, itemInHand);
                    return InteractionResult.SUCCESS;
                }

                if (!(item instanceof DyeItem dyeItem)) {
                    ItemStack copy = itemInHand.copy(); // retain a copy of the item before it is potentially consumed during animalInteract
                    InteractionResult animalInteractionResult = AnimalAccessor.cast(wolf).animalInteract(player, hand);
                    if(animalInteractionResult.consumesAction()){
                        AiUtil.animalEat(wolf, copy);
                    }
                    boolean willNotBreed = !animalInteractionResult.consumesAction() || wolf.isBaby();
                    if (willNotBreed) {
                        SharedWolfAi.manualCommand(wolf);
                        return InteractionResult.CONSUME;
                    }
                    return animalInteractionResult;
                }

                DyeColor dyeColor = dyeItem.getDyeColor();
                if (dyeColor != wolf.getCollarColor()) {
                    wolf.setCollarColor(dyeColor);
                    AnimalAccessor.cast(wolf).takeItemFromPlayer(player, hand, itemInHand);

                    return InteractionResult.SUCCESS;
                }
            }
            return AnimalAccessor.cast(wolf).animalInteract(player, hand);
        }
    }
}
