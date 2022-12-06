package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.util.AiHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LeapAtTarget extends Behavior<Mob> {
    public static final double MIN_LEAP_DISTANCE_SQR = 4.0D; // 2 * 2
    public static final double MAX_LEAP_DISTANCE_SQR = 16.0D; // 4 * 4
    private final double yDelta;

    private final boolean simple;

    public LeapAtTarget(double yDelta) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.yDelta = yDelta;
        this.simple = false;
    }

    public LeapAtTarget() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.yDelta = 0.0F;
        this.simple = true;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        if (mob.isVehicle()) {
            return false;
        } else {
            Optional<LivingEntity> optionalTarget = AiHelper.getAttackTarget(mob);
            if(optionalTarget.isPresent()){
                double distanceToTargetSqr = mob.distanceToSqr(optionalTarget.get());
                if (!(distanceToTargetSqr < MIN_LEAP_DISTANCE_SQR) && !(distanceToTargetSqr > MAX_LEAP_DISTANCE_SQR)) {
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
    public void start(ServerLevel level, Mob mob, long gameTime) {
        if(this.simple){
            mob.getJumpControl().jump();
        } else{
            LivingEntity target = AiHelper.getAttackTarget(mob).get();
            Vec3 deltaMovement = mob.getDeltaMovement();
            Vec3 positionDiff = new Vec3(target.getX() - mob.getX(), 0.0D, target.getZ() - mob.getZ());
            if (positionDiff.lengthSqr() > 1.0E-7D) {
                positionDiff = positionDiff.normalize().scale(0.4D).add(deltaMovement.scale(0.2D));
            }

            mob.setDeltaMovement(positionDiff.x, (double)this.yDelta, positionDiff.z);
        }
    }

    @Override
    public boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        return !mob.isOnGround();
    }
}
