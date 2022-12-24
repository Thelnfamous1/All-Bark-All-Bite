package com.infamous.call_of_the_wild.common.util;

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

    public static void maybeRetaliate(LivingEntity victim, List<? extends LivingEntity> allies, LivingEntity attacker, int angerTimeInTicks, double targetDistDiff) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(victim, attacker)) {
            if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(victim, attacker, targetDistDiff)) {
                if (attacker.getType() == EntityType.PLAYER && victim.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                    setAngerTargetToNearestTargetablePlayerIfFound(victim, attacker, angerTimeInTicks);
                    broadcastUniversalAnger(allies, angerTimeInTicks);
                } else {
                    setAngerTarget(victim, attacker, angerTimeInTicks);
                    broadcastAngerTarget(allies, attacker, angerTimeInTicks);
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

    private static void setAngerTarget(LivingEntity livingEntity, LivingEntity target, int angerTimeInTicks) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(livingEntity, target)) {
            Brain<?> brain = livingEntity.getBrain();
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            brain.setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, target.getUUID(), angerTimeInTicks);
            if(livingEntity instanceof NeutralMob neutralMob){
                neutralMob.setTarget(target);
                neutralMob.setRemainingPersistentAngerTime(angerTimeInTicks);
            }

            if (target.getType() == EntityType.PLAYER && livingEntity.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                brain.setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, angerTimeInTicks);
            }
        }
    }

    private static void broadcastUniversalAnger(List<? extends LivingEntity> allies, int angerTimeInTicks) {
        allies.forEach((adult) -> GenericAi.getNearestVisibleTargetablePlayer(adult).ifPresent((p) -> setAngerTarget(adult, p, angerTimeInTicks)));
    }

    public static void broadcastAngerTarget(List<? extends LivingEntity> allies, LivingEntity target, int angerTimeInTicks) {
        allies.forEach((d) -> setAngerTargetIfCloserThanCurrent(d, target, angerTimeInTicks));
    }

    private static void setAngerTargetIfCloserThanCurrent(LivingEntity livingEntity, LivingEntity target, int angerTimeInTicks) {
        Optional<LivingEntity> angerTarget = getAngerTarget(livingEntity);
        LivingEntity nearestTarget = BehaviorUtils.getNearestTarget(livingEntity, angerTarget, target);
        if (angerTarget.isEmpty() || angerTarget.get() != nearestTarget) {
            setAngerTarget(livingEntity, nearestTarget, angerTimeInTicks);
        }
    }
}
