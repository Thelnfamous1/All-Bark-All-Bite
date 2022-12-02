package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.util.AiHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LeapAtTarget extends Behavior<LivingEntity> {
    private final double yDelta;

    public LeapAtTarget(double yDelta) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.yDelta = yDelta;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, LivingEntity mob) {
        if (mob.isVehicle()) {
            return false;
        } else {
            Optional<LivingEntity> optionalTarget = AiHelper.getAttackTarget(mob);
            if(optionalTarget.isPresent()){
                double distanceToTargetSqr = mob.distanceToSqr(optionalTarget.get());
                if (!(distanceToTargetSqr < 4.0D) && !(distanceToTargetSqr > 16.0D)) {
                    if (!mob.isOnGround()) {
                        return false;
                    } else {
                        return mob.getRandom().nextInt(AiHelper.reducedTickDelay(5)) == 0;
                    }
                } else {
                    return false;
                }
            } else{
                return false;
            }
        }
    }

    @Override
    public void start(ServerLevel level, LivingEntity mob, long gameTime) {
        LivingEntity target = AiHelper.getAttackTarget(mob).get();
        Vec3 deltaMovement = mob.getDeltaMovement();
        Vec3 positionDiff = new Vec3(target.getX() - mob.getX(), 0.0D, target.getZ() - mob.getZ());
        if (positionDiff.lengthSqr() > 1.0E-7D) {
            positionDiff = positionDiff.normalize().scale(0.4D).add(deltaMovement.scale(0.2D));
        }

        mob.setDeltaMovement(positionDiff.x, (double)this.yDelta, positionDiff.z);
    }

    @Override
    public boolean canStillUse(ServerLevel level, LivingEntity mob, long gameTime) {
        return !mob.isOnGround();
    }
}
