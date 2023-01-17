package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import com.infamous.call_of_the_wild.common.util.MiscUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

@SuppressWarnings("NullableProblems")
public class LeapAtTarget extends Behavior<Mob> {
    private final float yD;
    private final int tooClose;
    private final int tooFar;

    public LeapAtTarget(float yD, int tooClose, int tooFar) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.yD = yD;
        this.tooClose = tooClose;
        this.tooFar = tooFar;
    }

    @Override
    public boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        if (mob.isVehicle()) {
            return false;
        } else {
            Optional<LivingEntity> attackTarget = GenericAi.getAttackTarget(mob);
            if(attackTarget.isPresent()){
                if (!mob.closerThan(attackTarget.get(), this.tooClose) && mob.closerThan(attackTarget.get(), this.tooFar)) {
                    if (this.isFloating(mob)) {
                        return false;
                    } else {
                        return MiscUtil.oneInChance(mob.getRandom(), 5);
                    }
                } else {
                    return false;
                }
            } else{
                return false;
            }
        }
    }

    private boolean isFloating(Mob mob) {
        return !mob.isOnGround();
    }

    @Override
    public void start(ServerLevel level, Mob mob, long gameTime) {
        Optional<LivingEntity> attackTarget = GenericAi.getAttackTarget(mob);
        attackTarget.ifPresent(target -> {
            Vec3 deltaMovement = mob.getDeltaMovement();
            Vec3 xzD = new Vec3(target.getX() - mob.getX(), 0.0D, target.getZ() - mob.getZ());
            if (xzD.lengthSqr() > 1.0E-7D) {
                xzD = xzD.normalize().scale(0.4D).add(deltaMovement.scale(0.2D));
            }

            mob.setDeltaMovement(xzD.x, this.yD, xzD.z);
        });
    }

    @Override
    public boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        return this.isFloating(mob);
    }
}
