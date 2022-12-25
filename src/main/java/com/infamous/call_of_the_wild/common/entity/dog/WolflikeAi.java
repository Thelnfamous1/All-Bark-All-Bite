package com.infamous.call_of_the_wild.common.entity.dog;

import com.infamous.call_of_the_wild.common.behavior.hunter.StartHunting;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.infamous.call_of_the_wild.common.util.HunterAi;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class WolflikeAi {
    static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    static final UniformInt ANGER_DURATION = TimeUtil.rangeOfSeconds(20, 39); // same as Wolf's persistent anger time
    static final UniformInt AVOID_DURATION = TimeUtil.rangeOfSeconds(5, 7);
    static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
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

    public static void initMemories(TamableAnimal tamableAnimal, RandomSource randomSource) {
        int huntCooldownInTicks = StartHunting.TIME_BETWEEN_HUNTS.sample(randomSource);
        HunterAi.setHuntedRecently(tamableAnimal, huntCooldownInTicks);
    }

    static boolean shouldPanic(LivingEntity livingEntity) {
        return livingEntity.isFreezing() || livingEntity.isOnFire();
    }

    static boolean isNearDisliked(LivingEntity livingEntity) {
        return GenericAi.isNearDisliked(livingEntity, DESIRED_DISTANCE_FROM_DISLIKED);
    }

    static boolean canAttack(TamableAnimal tamableAnimal) {
        return !tamableAnimal.isOrderedToSit() && !BehaviorUtils.isBreeding(tamableAnimal);
    }

    static boolean canAvoid(TamableAnimal tamableAnimal){
        return !tamableAnimal.isTame();
    }

    static boolean canFollowOwner(LivingEntity livingEntity) {
        return !BehaviorUtils.isBreeding(livingEntity);
    }

    static boolean canMakeLove(TamableAnimal tamableAnimal){
        return !tamableAnimal.isOrderedToSit();
    }

    static boolean canFollowNonOwner(TamableAnimal tamableAnimal) {
        return !tamableAnimal.isTame();
    }

    @SuppressWarnings("unused")
    static float getSpeedModifierTempted(LivingEntity livingEntity) {
        return SPEED_MODIFIER_TEMPTED;
    }

    static boolean canWander(TamableAnimal tamableAnimal){
        return !tamableAnimal.isOrderedToSit();
    }

    static Optional<? extends LivingEntity> findNearestValidAttackTarget(LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        Optional<LivingEntity> angryAt = BehaviorUtils.getLivingEntityFromUUIDMemory(livingEntity, MemoryModuleType.ANGRY_AT);
        if (angryAt.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(livingEntity, angryAt.get())) {
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

    static boolean wantsToRetaliate(TamableAnimal tamableAnimal, LivingEntity attacker) {
        LivingEntity owner = tamableAnimal.getOwner();
        if(owner == null) return true;
        return tamableAnimal.wantsToAttack(attacker, owner);
    }
}
