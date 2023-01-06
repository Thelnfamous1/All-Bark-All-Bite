package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.infamous.call_of_the_wild.common.util.LongJumpAi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class StalkPrey<E extends PathfinderMob> extends Behavior<E> {
    public static final int POUNCE_DELAY_TICKS = 15;
    private final BiPredicate<E, LivingEntity> wantsToStalk;
    private final float speedModifier;
    private final int closeEnough;
    private final Predicate<E> isPreparingToPounce;
    private final BiConsumer<E, Boolean> toggleIsPreparingToPounce;
    private final double maxJumpVelocity;
    private int calculatePathCounter;
    private int crouchAnimationTimer;
    private StalkPrey.State state = StalkPrey.State.DONE;

    public StalkPrey(BiPredicate<E, LivingEntity> wantsToStalk, float speedModifier, int closeEnough, Predicate<E> isPreparingToPounce, BiConsumer<E, Boolean> toggleIsPreparingToPounce, double maxJumpVelocity) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ABABMemoryModuleTypes.IS_STALKING.get(), MemoryStatus.REGISTERED,
                ABABMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
        this.wantsToStalk = wantsToStalk;
        this.speedModifier = speedModifier;
        this.closeEnough = closeEnough;
        this.isPreparingToPounce = isPreparingToPounce;
        this.toggleIsPreparingToPounce = toggleIsPreparingToPounce;
        this.maxJumpVelocity = maxJumpVelocity;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        LivingEntity target = this.getStalkTarget(mob).get();
        return this.canStalk(mob, target) && !mob.closerThan(target, this.closeEnough);
    }

    private Optional<LivingEntity> getStalkTarget(E mob) {
        return GenericAi.getAttackTarget(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        this.setWalkAndLookTarget(mob, this.getStalkTarget(mob).get());
        this.calculatePathCounter = 10;
        mob.getBrain().setMemory(ABABMemoryModuleTypes.IS_STALKING.get(), Unit.INSTANCE);
        this.state = StalkPrey.State.MOVE_TO_TARGET;
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        Optional<LivingEntity> stalkTarget = this.getStalkTarget(mob);
        if(stalkTarget.isPresent()){
            LivingEntity target = stalkTarget.get();
            switch (this.state){
                case MOVE_TO_TARGET -> {
                    return this.canStalk(mob, target);
                }
                case CROUCH_ANIMATION -> {
                    return true;
                }
                case POUNCE -> {
                    return this.canPounce(mob, target);
                }
                case DONE -> {
                    return false;
                }
                default -> throw new IllegalStateException("Invalid StalkPrey.State: " + this.state);
            }
        } else{
            return false;
        }
    }

    private boolean canStalk(E mob, LivingEntity target) {
        return target.isAlive()
                && this.wantsToStalk.test(mob, target)
                && !this.isPreparingToPounce.test(mob);
    }

    private boolean canPounce(E mob, LivingEntity target){
        return target.isAlive()
                && LongJumpAi.calculateOptimalJumpVector(mob, target.position(), this.maxJumpVelocity, LongJumpAi.ALLOWED_ANGLES) != null;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void tick(ServerLevel level, E mob, long gameTime) {
        LivingEntity target = this.getStalkTarget(mob).get();
        switch (this.state){
            case MOVE_TO_TARGET -> {
                if (mob.closerThan(target, this.closeEnough)) {
                    this.toggleIsPreparingToPounce.accept(mob, true);
                    this.clearWalkTarget(mob);
                    this.state = State.CROUCH_ANIMATION;
                    this.crouchAnimationTimer = 0;
                } else if (this.calculatePathCounter <= 0) {
                    this.setWalkAndLookTarget(mob, target);
                    this.calculatePathCounter = 10;
                } else {
                    --this.calculatePathCounter;
                }
            }
            case CROUCH_ANIMATION -> {
                if (this.crouchAnimationTimer++ >= POUNCE_DELAY_TICKS) {
                    this.state = State.POUNCE;
                }
            }
            case POUNCE -> {
                BlockPos targetPos = target.blockPosition();
                LongJumpAi.setLongJumpTarget(mob, new BlockPosTracker(targetPos));
                this.lookAtTarget(mob, target);
                mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                this.state = State.DONE;
            }
            case DONE -> {
            }
        }
    }

    private void clearWalkTarget(E mob) {
        GenericAi.stopWalking(mob);
    }

    private void setWalkAndLookTarget(E mob, LivingEntity target) {
        Brain<?> brain = mob.getBrain();
        this.lookAtTarget(mob, target);
        WalkTarget walkTarget = new WalkTarget(new EntityTracker(target, true), this.speedModifier, this.closeEnough - 1);
        brain.setMemory(MemoryModuleType.WALK_TARGET, walkTarget);
    }

    private void lookAtTarget(E mob, LivingEntity target) {
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
    }

    @Override
    protected void stop(ServerLevel level, E mob, long gameTime) {
        this.toggleIsPreparingToPounce.accept(mob, false);
        mob.getBrain().eraseMemory(ABABMemoryModuleTypes.IS_STALKING.get());
        this.state = State.DONE;
    }

    enum State {
        MOVE_TO_TARGET,
        CROUCH_ANIMATION,
        POUNCE,
        DONE
    }

}
