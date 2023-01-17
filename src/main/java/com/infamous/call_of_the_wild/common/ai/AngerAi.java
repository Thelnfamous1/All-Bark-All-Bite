package com.infamous.call_of_the_wild.common.ai;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

import java.util.List;
import java.util.Optional;

public class AngerAi {

    public static Optional<LivingEntity> getAngerTarget(LivingEntity livingEntity) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(livingEntity, MemoryModuleType.ANGRY_AT);
    }

    public static void maybeRetaliate(LivingEntity victim, List<? extends LivingEntity> toAlert, LivingEntity attacker, UniformInt angerTime, double tooFar) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(victim, attacker)) {
            if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(victim, attacker, tooFar)) {
                int angerTimeInTicks = angerTime.sample(victim.getRandom());
                if (attacker.getType() == EntityType.PLAYER && victim.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                    setAngerTargetToNearestTargetablePlayerIfFound(victim, attacker, angerTimeInTicks);
                    broadcastUniversalAnger(toAlert, angerTime);
                } else {
                    setAngerTarget(victim, attacker, angerTimeInTicks);
                    broadcastAngerTarget(toAlert, attacker, angerTime);
                }
            }
        }
    }

    private static void setAngerTargetToNearestTargetablePlayerIfFound(LivingEntity livingEntity, LivingEntity target, int angerTimeInTicks) {
        Optional<Player> optional = GenericAi.getNearestVisibleTargetablePlayer(livingEntity);
        if (optional.isPresent()) {
            setAngerTarget(livingEntity, optional.get(), angerTimeInTicks);
        } else {
            setAngerTarget(livingEntity, target, angerTimeInTicks);
        }
    }

    public static void setAngerTarget(LivingEntity livingEntity, LivingEntity target, int angerTimeInTicks) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(livingEntity, target)) {
            Brain<?> brain = livingEntity.getBrain();
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            brain.setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, target.getUUID(), angerTimeInTicks);
            if(livingEntity instanceof NeutralMob neutralMob){
                neutralMob.setTarget(target);
                neutralMob.setPersistentAngerTarget(target.getUUID());
                neutralMob.setRemainingPersistentAngerTime(angerTimeInTicks);
            }

            if (target.getType() == EntityType.PLAYER && livingEntity.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                brain.setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, angerTimeInTicks);
            }
        }
    }

    private static void broadcastUniversalAnger(List<? extends LivingEntity> alertables, UniformInt angerTime) {
        alertables.forEach((adult) -> GenericAi.getNearestVisibleTargetablePlayer(adult).ifPresent((p) -> setAngerTarget(adult, p, angerTime.sample(p.getRandom()))));
    }

    public static void broadcastAngerTarget(List<? extends LivingEntity> alertables, LivingEntity target, UniformInt angerTime) {
        alertables.forEach((alertable) -> setAngerTargetIfCloserThanCurrent(alertable, target, angerTime.sample(alertable.getRandom())));
    }

    public static void setAngerTargetIfCloserThanCurrent(LivingEntity mob, LivingEntity target, int angerTimeInTicks) {
       Optional<LivingEntity> optional = getAngerTarget(mob);
       LivingEntity livingentity = BehaviorUtils.getNearestTarget(mob, optional, target);
       if (optional.isEmpty() || optional.get() != livingentity) {
          setAngerTarget(mob, livingentity, angerTimeInTicks);
       }
    }

    public static boolean hasAngryAt(LivingEntity entity) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.ANGRY_AT);
    }
}
