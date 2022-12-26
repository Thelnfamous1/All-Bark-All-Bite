package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class JumpAtTarget extends Behavior<Mob> {
    public static final double MIN_LEAP_DISTANCE_SQR = 4.0D; // 2 * 2
    public static final double MAX_LEAP_DISTANCE_SQR = 16.0D; // 4 * 4
    private static final long LEAP_INTERVAL = 100L;

    private long lastLeapTimestamp;

    public JumpAtTarget() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        if (mob.isVehicle()) {
            return false;
        } else {
            Optional<LivingEntity> optionalTarget = GenericAi.getAttackTarget(mob);
            if(optionalTarget.isPresent()){
                double distanceToTargetSqr = mob.distanceToSqr(optionalTarget.get());
                if (!(distanceToTargetSqr < MIN_LEAP_DISTANCE_SQR) && !(distanceToTargetSqr > MAX_LEAP_DISTANCE_SQR)) {
                    if (this.canJump(mob, level.getGameTime())) {
                        return false;
                    } else {
                        return mob.getRandom().nextInt(AiUtil.reducedTickDelay(5)) == 0;
                    }
                } else {
                    return false;
                }
            } else{
                return false;
            }
        }
    }

    private boolean canJump(Mob mob, long gameTime) {
        return !mob.isOnGround() && gameTime >= this.lastLeapTimestamp + LEAP_INTERVAL;
    }

    @Override
    public void start(ServerLevel level, Mob mob, long gameTime) {
        mob.getJumpControl().jump();
        this.lastLeapTimestamp = gameTime;
    }

    @Override
    public boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        return this.canJump(mob, gameTime);
    }
}
