package com.infamous.all_bark_all_bite.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import com.infamous.all_bark_all_bite.common.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.ai.HunterAi;
import com.infamous.all_bark_all_bite.common.ai.LongJumpAi;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.ToIntFunction;

public class Pounce extends Behavior<PathfinderMob> {

    private static final double MIN_Y_DELTA = 0.05D;
    private final ToIntFunction<PathfinderMob> pounceHeight;
    private final ToIntFunction<PathfinderMob> pounceDistance;
    private final ToIntFunction<PathfinderMob> pounceCooldown;

    public Pounce(ToIntFunction<PathfinderMob> pounceDistance, ToIntFunction<PathfinderMob> pounceHeight, ToIntFunction<PathfinderMob> pounceCooldown) {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.POUNCE_TARGET.get(), MemoryStatus.VALUE_PRESENT,
                ABABMemoryModuleTypes.POUNCE_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT
        ));
        this.pounceDistance = pounceDistance;
        this.pounceHeight = pounceHeight;
        this.pounceCooldown = pounceCooldown;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
        if (!mob.hasPose(Pose.CROUCHING)) {
            HunterAi.stopPouncing(mob);
            return false;
        } else {
            LivingEntity pounceTarget = this.getPounceTarget(mob).orElse(null);
            if (pounceTarget != null && pounceTarget.isAlive()) {
                if (pounceTarget.getMotionDirection() != pounceTarget.getDirection()) {
                    HunterAi.stopPouncing(mob);
                    return false;
                } else {
                    boolean pathClear = AiUtil.isPathClear(mob, pounceTarget, this.pounceDistance.applyAsInt(mob), this.pounceHeight.applyAsInt(mob));
                    if (!pathClear) {
                        //mob.getNavigation().createPath(pounceTarget, 0);
                        HunterAi.stopPouncing(mob);
                        if(mob.hasPose(Pose.CROUCHING)) mob.setPose(Pose.STANDING);
                    }

                    return pathClear;
                }
            } else {
                HunterAi.stopPouncing(mob);
                return false;
            }
        }
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        //mob.setJumping(true);
        mob.setPose(Pose.LONG_JUMPING);
        LongJumpAi.setMidJump(mob);
        LivingEntity pounceTarget = this.getPounceTarget(mob).orElse(null);
        if (pounceTarget != null) {
            BehaviorUtils.lookAtEntity(mob, pounceTarget);
            Vec3 jumpVector = pounceTarget.position().subtract(mob.position()).normalize();
            double xzDScale = this.pounceDistance.applyAsInt(mob) * 2.0D / 15.0D; // 0.8D for 6
            double yD = this.pounceHeight.applyAsInt(mob) * 3.0D / 10.0D; // 0.9D for 3
            mob.setDeltaMovement(mob.getDeltaMovement().add(jumpVector.x * xzDScale, yD, jumpVector.z * xzDScale));
        }

        GenericAi.stopWalking(mob);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob mob, long gameTime) {
        double yD = mob.getDeltaMovement().y;
        return (Mth.square(yD) >= MIN_Y_DELTA || !mob.isOnGround());
    }

    @NotNull
    private Optional<LivingEntity> getPounceTarget(PathfinderMob mob) {
        return HunterAi.getPounceTarget(mob);
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void tick(ServerLevel level, PathfinderMob mob, long gameTime) {
        this.getPounceTarget(mob).ifPresent(pounceTarget -> BehaviorUtils.lookAtEntity(mob, pounceTarget));
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob mob, long gameTime) {
        if(mob.hasPose(Pose.LONG_JUMPING)){
            mob.setPose(Pose.STANDING);
        }
        HunterAi.stopPouncing(mob);
        LongJumpAi.clearMidJump(mob);
        HunterAi.setPounceCooldown(mob, this.pounceCooldown.applyAsInt(mob));
    }
}
