package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.infamous.call_of_the_wild.common.util.HunterAi;
import com.infamous.call_of_the_wild.common.util.LongJumpAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

@SuppressWarnings("NullableProblems")
public class StalkPrey<E extends PathfinderMob> extends Behavior<E> {
    public static final int POUNCE_DELAY_TICKS = 15;
    private final BiPredicate<E, LivingEntity> wantsToStalk;
    private final float stalkSpeedModifier;
    private final float walkSpeedModifier;
    private final int closeEnough;
    private final int tooFar;
    private final Function<E, Boolean> isInterested;
    private final BiConsumer<E, Boolean> toggleInterest;
    private final double maxJumpVelocity;

    public StalkPrey(BiPredicate<E, LivingEntity> wantsToStalk, float stalkSpeedModifier, float walkSpeedModifier, int closeEnough, int tooFar, Function<E, Boolean> isInterested, BiConsumer<E, Boolean> toggleInterest, double maxJumpVelocity) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.STALK_TARGET.get(), MemoryStatus.VALUE_PRESENT,
                COTWMemoryModuleTypes.POUNCE_DELAY.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED

        ));
        this.wantsToStalk = wantsToStalk;
        this.stalkSpeedModifier = stalkSpeedModifier;
        this.walkSpeedModifier = walkSpeedModifier;
        this.closeEnough = closeEnough;
        this.tooFar = tooFar;
        this.isInterested = isInterested;
        this.toggleInterest = toggleInterest;
        this.maxJumpVelocity = maxJumpVelocity;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        LivingEntity target = this.getStalkTarget(mob).get();
        if(this.canStalk(mob, target)){
            this.lookAtTarget(mob, target);
            if (mob.distanceToSqr(target) <= Mth.square(this.closeEnough)) {
                this.setIsPreparingToPounce(mob, true);
                this.clearWalkTarget(mob);
            } else {
                this.setWalkAndLookTarget(mob, target);
            }
        } else{
            if (LongJumpAi.calculateOptimalJumpVector(mob, target.position(), this.maxJumpVelocity, LongJumpAi.ALLOWED_ANGLES) != null) {
                this.setIsPreparingToPounce(mob, true);
                this.clearWalkTarget(mob);
                this.lookAtTarget(mob, target);
            } else {
                this.setIsPreparingToPounce(mob, false);
                mob.getBrain().eraseMemory(COTWMemoryModuleTypes.STALK_TARGET.get());
            }
        }
    }

    private void lookAtTarget(E mob, LivingEntity target) {
        AiUtil.lookAtTargetIgnoreLineOfSight(mob, target);
    }

    private void setIsPreparingToPounce(E mob, boolean isPreparing) {
        this.toggleInterest.accept(mob, isPreparing);
        if(!isPreparing){
            if(mob.hasPose(Pose.CROUCHING)){
                mob.setPose(Pose.STANDING);
                mob.getBrain().eraseMemory(COTWMemoryModuleTypes.POUNCE_DELAY.get());
            }
        } else{
            if(!mob.hasPose(Pose.CROUCHING)){
                mob.setPose(Pose.CROUCHING);
                mob.getBrain().setMemoryWithExpiry(COTWMemoryModuleTypes.POUNCE_DELAY.get(), Unit.INSTANCE, POUNCE_DELAY_TICKS);
            }
        }
    }

    private Optional<LivingEntity> getStalkTarget(E mob) {
        return HunterAi.getStalkTarget(mob);
    }

    private boolean canStalk(E mob, LivingEntity target) {
        if (mob.isSleeping()) {
            return false;
        } else {
            return target.isAlive()
                    && this.wantsToStalk.test(mob, target)
                    && mob.distanceToSqr(target) > Mth.square(this.closeEnough)
                    && this.isPreparingToPounce(mob);
                    //&& !AiUtil.isJumping(mob);
        }
    }

    private boolean isPreparingToPounce(E mob) {
        return !mob.hasPose(Pose.CROUCHING) && !this.isInterested.apply(mob);
    }

    private void setWalkAndLookTarget(E mob, LivingEntity target) {
        Brain<?> brain = mob.getBrain();
        this.lookAtTarget(mob, target);
        boolean tooFar = !mob.closerThan(target, this.tooFar);

        brain.getMemory(MemoryModuleType.WALK_TARGET).ifPresent(wt -> {
            float wtSpeedModifier = wt.getSpeedModifier();
            if(tooFar && wtSpeedModifier != this.walkSpeedModifier && wtSpeedModifier == this.stalkSpeedModifier){
                mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
            } else if(wtSpeedModifier != this.stalkSpeedModifier && wtSpeedModifier == this.walkSpeedModifier){
                mob.getNavigation().setSpeedModifier(this.stalkSpeedModifier);
            }
        });

        WalkTarget walkTarget = new WalkTarget(
                new EntityTracker(target, false),
                tooFar ? this.walkSpeedModifier : this.stalkSpeedModifier,
                this.closeEnough);
        brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    private void clearWalkTarget(E mob) {
        GenericAi.stopWalking(mob);
    }

}
