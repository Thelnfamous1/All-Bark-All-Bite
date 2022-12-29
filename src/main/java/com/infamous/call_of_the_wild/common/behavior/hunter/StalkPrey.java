package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.HunterAi;
import com.infamous.call_of_the_wild.common.util.LongJumpAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
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
    private final float speedModifier;
    private final int closeEnough;
    private final Function<E, Boolean> isInterested;
    private final BiConsumer<E, Boolean> toggleInterest;
    private final double maxJumpVelocity;

    public StalkPrey(BiPredicate<E, LivingEntity> wantsToStalk, float speedModifier, int closeEnough, Function<E, Boolean> isInterested, BiConsumer<E, Boolean> toggleInterest, double maxJumpVelocity) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.STALK_TARGET.get(), MemoryStatus.VALUE_PRESENT,
                COTWMemoryModuleTypes.POUNCE_DELAY.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED

        ));
        this.wantsToStalk = wantsToStalk;
        this.speedModifier = speedModifier;
        this.closeEnough = closeEnough;
        this.isInterested = isInterested;
        this.toggleInterest = toggleInterest;
        this.maxJumpVelocity = maxJumpVelocity;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        LivingEntity target = this.getStalkTarget(mob).get();
        if(this.canStalk(mob, target)){
            BehaviorUtils.lookAtEntity(mob, target);
            if (mob.closerThan(target, this.closeEnough)) {
                this.setIsPreparingToPounce(mob, true);
                this.clearWalkTarget(mob);
            } else {
                this.setWalkAndLookTarget(mob, target);
            }
        } else{
            if (LongJumpAi.calculateOptimalJumpVector(mob, target.position(), this.maxJumpVelocity, LongJumpAi.ALLOWED_ANGLES) != null) {
                this.setIsPreparingToPounce(mob, true);
                this.clearWalkTarget(mob);
                BehaviorUtils.lookAtEntity(mob, target);
            } else {
                this.setIsPreparingToPounce(mob, false);
            }
        }
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
                    && !mob.closerThan(target, this.closeEnough)
                    && this.isNotStalking(mob);
                    //&& !AiUtil.isJumping(mob);
        }
    }

    private boolean isNotStalking(E mob) {
        return !mob.hasPose(Pose.CROUCHING) && !this.isInterested.apply(mob);
    }

    private void setWalkAndLookTarget(LivingEntity mob, LivingEntity target) {
        Brain<?> brain = mob.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
        WalkTarget walkTarget = new WalkTarget(new EntityTracker(target, false), this.speedModifier, this.closeEnough);
        brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    private void clearWalkTarget(LivingEntity mob) {
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

}
