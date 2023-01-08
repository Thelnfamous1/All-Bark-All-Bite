package com.infamous.call_of_the_wild.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;
import java.util.UUID;

public class AiUtil {

    private static final String LIVING_ENTITY_JUMPING = "f_20899_";
    private static final String LIVING_ENTITY_GET_SOUND_VOLUME = "m_6121_";

    public static int reducedTickDelay(int ticks) {
        return Mth.positiveCeilDiv(ticks, 2);
    }

    public static double getFollowRange(Mob mob) {
        return mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    public static void addEatEffect(LivingEntity eater, Level level, FoodProperties foodProperties) {
        for(Pair<MobEffectInstance, Float> pair : foodProperties.getEffects()) {
            if (!level.isClientSide && pair.getFirst() != null && level.random.nextFloat() < pair.getSecond()) {
                eater.addEffect(new MobEffectInstance(pair.getFirst()));
            }
        }
    }

    public static boolean hasAnyMemory(LivingEntity livingEntity, MemoryModuleType<?>... memoryModuleTypes){
        Brain<?> brain = livingEntity.getBrain();
        for(MemoryModuleType<?> memoryModuleType : memoryModuleTypes){
            if(brain.hasMemoryValue(memoryModuleType)) return true;
        }
        return false;
    }

    public static void eraseAllMemories(LivingEntity livingEntity, MemoryModuleType<?>... memoryModuleTypes) {
        Brain<?> brain = livingEntity.getBrain();
        for (MemoryModuleType<?> memoryModuleType : memoryModuleTypes) {
            brain.eraseMemory(memoryModuleType);
        }
    }

    public static boolean isHostile(Mob mob, LivingEntity target, int closeEnough, TagKey<EntityType<?>> alwaysHostiles, boolean requireLineOfSight){
        return isClose(mob, target, closeEnough)
                && target.getType().is(alwaysHostiles)
                && isAttackable(mob, target, requireLineOfSight);
    }

    public static boolean isClose(Mob mob, LivingEntity target, int closeEnough) {
        return target.distanceToSqr(mob) <= Mth.square(closeEnough);
    }

    public static boolean isHuntable(Mob mob, LivingEntity target, int closeEnough, TagKey<EntityType<?>> huntTargets, boolean requireLineOfSight){
        return isClose(mob, target, closeEnough)
                && isHuntTarget(mob, target, huntTargets)
                && isAttackable(mob, target, requireLineOfSight);
    }

    private static boolean isAttackable(Mob mob, LivingEntity target, boolean requireLineOfSight){
        return requireLineOfSight ? Sensor.isEntityAttackable(mob, target) : Sensor.isEntityAttackableIgnoringLineOfSight(mob, target);
    }

    @SuppressWarnings("unused")
    public static boolean isHuntTarget(LivingEntity mob, LivingEntity target, TagKey<EntityType<?>> huntTargets) {
        return //!hasAnyMemory(mob, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.HAS_HUNTING_COOLDOWN) &&
            target.getType().is(huntTargets);
    }

    public static boolean isHuntableBabyTurtle(Mob mob, LivingEntity target, int closeEnough, boolean requireLineOfSight) {
        return isClose(mob, target, closeEnough)
                && target instanceof Turtle turtle
                && Turtle.BABY_ON_LAND_SELECTOR.test(turtle)
                && isAttackable(mob, turtle, requireLineOfSight);
    }

    public static boolean canBeConsideredAnAlly(LivingEntity mob, LivingEntity other) {
        return (mob instanceof OwnableEntity ownable && other instanceof OwnableEntity ownableOther)
                    && ownable.getOwner() == ownableOther.getOwner()
                || mob.isAlliedTo(other)
                || (mob.getType() == other.getType() || mob.getMobType() == other.getMobType())
                    && mob.getTeam() == null && other.getTeam() == null;
    }

    @SuppressWarnings("unused")
    public static boolean isJumping(LivingEntity livingEntity) {
        return ReflectionUtil.getField(LIVING_ENTITY_JUMPING, LivingEntity.class, livingEntity);
    }

    public static Optional<LivingEntity> getLivingEntityFromUUID(ServerLevel level, UUID uuid) {
        return Optional.of(uuid).map(level::getEntity).filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isLookingAtMe(LivingEntity me, LivingEntity target, double offset) {
        Vec3 viewVector = target.getViewVector(1.0F).normalize();
        Vec3 eyeToMyEyeVector = target.getEyePosition().vectorTo(me.getEyePosition());
        double distance = eyeToMyEyeVector.length();
        eyeToMyEyeVector = eyeToMyEyeVector.normalize();
        double dot = viewVector.dot(eyeToMyEyeVector);
        return dot > 1.0D - offset / distance && target.hasLineOfSight(me);
    }

    public static <T extends Entity> InteractionResult interactOn(Player player, T entity, InteractionHand hand, TriFunction<T, Player, InteractionHand, InteractionResult> interactCallback){
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack itemInHandCopy = itemInHand.copy();
        InteractionResult interactionResult = interactCallback.apply(entity, player, hand); // replaces Entity#interact(Player, InteractionHand);
        if (interactionResult.consumesAction()) {
            if (player.getAbilities().instabuild && itemInHand == player.getItemInHand(hand) && itemInHand.getCount() < itemInHandCopy.getCount()) {
                itemInHand.setCount(itemInHandCopy.getCount());
            }

            if (!player.getAbilities().instabuild && itemInHand.isEmpty()) {
                ForgeEventFactory.onPlayerDestroyItem(player, itemInHandCopy, hand);
            }
            return interactionResult;
        } else {
            if (!itemInHand.isEmpty() && entity instanceof LivingEntity) {
                if (player.getAbilities().instabuild) {
                    itemInHand = itemInHandCopy;
                }

                InteractionResult interactLivingEntity = itemInHand.interactLivingEntity(player, (LivingEntity)entity, hand);
                if (interactLivingEntity.consumesAction()) {
                    if (itemInHand.isEmpty() && !player.getAbilities().instabuild) {
                        ForgeEventFactory.onPlayerDestroyItem(player, itemInHandCopy, hand);
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    }

                    return interactLivingEntity;
                }
            }

            return InteractionResult.PASS;
        }
    }

    public static boolean isInjured(LivingEntity livingEntity){
        return livingEntity.getHealth() < livingEntity.getMaxHealth();
    }

    public static void animalEat(Animal animal, ItemStack stack) {
        if (animal.isFood(stack) && !animal.level.isClientSide) {
            playSoundEvent(animal, animal.getEatingSound(stack));

            float healAmount = 1.0F;
            FoodProperties foodProperties = stack.getFoodProperties(animal);
            if(foodProperties != null){
                healAmount = foodProperties.getNutrition();
                addEatEffect(animal, animal.level, foodProperties);
            }
            if(isInjured(animal)) animal.heal(healAmount);

            animal.ate();
        }
    }

    public static void playSoundEvent(LivingEntity livingEntity, SoundEvent soundEvent) {
        livingEntity.playSound(soundEvent, getSoundVolume(livingEntity), livingEntity.getVoicePitch());
    }

    private static float getSoundVolume(LivingEntity livingEntity) {
        return ReflectionUtil.callMethod(LIVING_ENTITY_GET_SOUND_VOLUME, livingEntity);
    }
}
