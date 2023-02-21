package com.infamous.all_bark_all_bite.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.util.AiUtil;
import com.infamous.all_bark_all_bite.common.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.ai.HunterAi;
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

import java.util.Optional;
import java.util.function.ToIntFunction;

public class StalkPrey extends Behavior<PathfinderMob> {
    private static final int CROUCH_ANIMATION_DURATION = 15;
    private static final int PATH_INTERVAL = 10;
    public static final double INITIAL_VISION_OFFSET = 0.5D; // dot initially only needs to be greater than 1 - (0.5 / 0.5) == 0
    private final float speedModifier;
    private final ToIntFunction<PathfinderMob> pounceDistance;
    private final ToIntFunction<PathfinderMob> pounceHeight;
    private int calculatePathCounter;
    private int crouchAnimationTimer;
    private StalkPrey.State state = StalkPrey.State.DONE;

    public StalkPrey(float speedModifier, ToIntFunction<PathfinderMob> pounceDistance, ToIntFunction<PathfinderMob> pounceHeight) {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.STALK_TARGET.get(), MemoryStatus.VALUE_PRESENT,
                ABABMemoryModuleTypes.POUNCE_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
        this.speedModifier = speedModifier;
        this.pounceDistance = pounceDistance;
        this.pounceHeight = pounceHeight;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
        boolean canStalk = this.canStalk(mob);
        if(!canStalk){
            HunterAi.stopStalking(mob);
        }
        return canStalk;
    }

    @Override
    protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
        this.calculatePathCounter = 10;
        this.state = StalkPrey.State.MOVE_TO_TARGET;
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, PathfinderMob mob, long gameTime) {
        switch (this.state){
            case MOVE_TO_TARGET -> {
                return this.canStalk(mob);
            }
            case CROUCH_ANIMATION -> {
                return this.canPounce(mob);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean canStalk(PathfinderMob mob) {
        if (mob.isSleeping()) {
            return false;
        } else {
            LivingEntity stalkTarget = this.getStalkTarget(mob).orElse(null);
            return stalkTarget != null
                    && stalkTarget.isAlive()
                    && !AiUtil.isLookingAtMe(mob, stalkTarget, INITIAL_VISION_OFFSET)
                    && HunterAi.getPounceTarget(mob).isEmpty()
                    //&& mob.distanceToSqr(stalkTarget) >= Mth.square(this.pounceDistance)
                    && !mob.hasPose(Pose.CROUCHING);
        }
    }

    private boolean canPounce(PathfinderMob mob) {
        LivingEntity stalkTarget = this.getStalkTarget(mob).orElse(null);
        return stalkTarget != null && stalkTarget.isAlive() && !AiUtil.isLookingAtMe(mob, stalkTarget, INITIAL_VISION_OFFSET);
    }

    private Optional<LivingEntity> getStalkTarget(PathfinderMob mob) {
        return HunterAi.getStalkTarget(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void tick(ServerLevel level, PathfinderMob mob, long gameTime) {
        LivingEntity stalkTarget = this.getStalkTarget(mob).get();
        BehaviorUtils.lookAtEntity(mob, stalkTarget);
        switch (this.state){
            case MOVE_TO_TARGET -> {
                if (mob.distanceToSqr(stalkTarget) <= Mth.square(this.pounceDistance.applyAsInt(mob))) {
                    mob.setPose(Pose.CROUCHING);
                    GenericAi.stopWalking(mob);
                    this.crouchAnimationTimer = 0;
                    this.state = State.CROUCH_ANIMATION;
                } else if (this.calculatePathCounter <= 0) {
                    this.setWalkAndLookTarget(mob, stalkTarget);
                    this.calculatePathCounter = PATH_INTERVAL;
                } else {
                    --this.calculatePathCounter;
                }
            }
            case CROUCH_ANIMATION -> {
                if (this.crouchAnimationTimer++ >= CROUCH_ANIMATION_DURATION) {
                    this.state = State.DONE;
                }
            }
            case DONE -> {
            }
        }
    }

    private void setWalkAndLookTarget(PathfinderMob mob, LivingEntity target) {
        AiUtil.setWalkAndLookTargetMemories(mob, target, this.speedModifier, this.pounceDistance.applyAsInt(mob) - 1);
    }

    @Override
    protected void stop(ServerLevel level, PathfinderMob mob, long gameTime) {
        LivingEntity stalkTarget = this.getStalkTarget(mob).orElse(null);
        if (this.state == State.DONE) {
            if (stalkTarget != null && AiUtil.isPathClear(mob, stalkTarget, this.pounceDistance.applyAsInt(mob), this.pounceHeight.applyAsInt(mob))) {
                HunterAi.setPounceTarget(mob, stalkTarget);
                GenericAi.stopWalking(mob);
                BehaviorUtils.lookAtEntity(mob, stalkTarget);
            } else {
                if (mob.hasPose(Pose.CROUCHING)) mob.setPose(Pose.STANDING);
            }
        } else {
            this.state = State.DONE;
            if (mob.hasPose(Pose.CROUCHING)) mob.setPose(Pose.STANDING);
        }
        HunterAi.stopStalking(mob);
    }

    enum State {
        MOVE_TO_TARGET,
        CROUCH_ANIMATION,
        DONE
    }

}
