package com.infamous.all_bark_all_bite.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import com.infamous.all_bark_all_bite.common.util.ai.HunterAi;
import com.infamous.all_bark_all_bite.common.util.ai.LongJumpAi;
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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class StalkAndPounce<E extends PathfinderMob> extends Behavior<E> {
    private static final int CROUCH_ANIMATION_DURATION = 15;
    private static final int PATH_INTERVAL = 10;
    public static final double INITIAL_VISION_OFFSET = 0.5D; // dot initially only needs to be greater than 1 - (0.5 / 0.5) == 0
    private static final double MIN_Y_DELTA = 0.05D;
    private static final long TIMEOUT_TO_GET_WITHIN_POUNCE_RANGE = 200L;
    private final float speedModifier;
    private final int pounceDistance;
    private final int pounceHeight;
    private int calculatePathCounter;
    private int crouchAnimationTimer;
    private StalkAndPounce.State state = StalkAndPounce.State.DONE;
    private final BiPredicate<E, LivingEntity> stopStalkingWhen;
    private final BiConsumer<E, LivingEntity> onTargetErased;

    public StalkAndPounce(float speedModifier, int pounceDistance, int pounceHeight, BiPredicate<E, LivingEntity> stopStalkingWhen){
        this(speedModifier, pounceDistance, pounceHeight, stopStalkingWhen, (mob, le) -> {});
    }

    public StalkAndPounce(float speedModifier, int pounceDistance, int pounceHeight, BiPredicate<E, LivingEntity> stopStalkingWhen, BiConsumer<E, LivingEntity> onTargetErased) {
        super(ImmutableMap.of(
                ABABMemoryModuleTypes.HUNT_TARGET.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
        this.speedModifier = speedModifier;
        this.pounceDistance = pounceDistance;
        this.pounceHeight = pounceHeight;
        this.stopStalkingWhen = stopStalkingWhen;
        this.onTargetErased = onTargetErased;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        boolean canStalk = this.canStalk(mob) && mob.distanceToSqr(this.getStalkTarget(mob).get()) > Mth.square(this.pounceDistance);
        if(!canStalk){
            this.clearTarget(mob);
        }
        return canStalk;
    }

    private void clearTarget(E mob) {
        this.getStalkTarget(mob).ifPresent(stalkTarget -> this.onTargetErased.accept(mob, stalkTarget));
        HunterAi.stopHunting(mob);
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        this.calculatePathCounter = 10;
        this.state = StalkAndPounce.State.STALK;
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        switch (this.state){
            case STALK -> {
                return this.canStalk(mob);
            }
            case CROUCH_ANIMATION -> {
                return this.canCrouch(mob);
            }
            case POUNCE -> {
                return this.canPounce(mob);
            }
            case MID_POUNCE -> {
                return this.isMidPounce(mob);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean canStalk(E mob) {
        if (mob.isSleeping()) {
            return false;
        } else {
            LivingEntity stalkTarget = this.getStalkTarget(mob).orElse(null);
            return this.isValidStalkTarget(mob, stalkTarget)
                    && !mob.hasPose(Pose.CROUCHING);
        }
    }

    private boolean isValidStalkTarget(E mob, @Nullable LivingEntity stalkTarget) {
        return stalkTarget != null
                && stalkTarget.isAlive()
                && mob.level() == stalkTarget.level()
                && !AiUtil.isTiredOfTryingToReachTarget(mob, TIMEOUT_TO_GET_WITHIN_POUNCE_RANGE)
                && !AiUtil.isLookingAtMe(mob, stalkTarget, INITIAL_VISION_OFFSET)
                && !this.stopStalkingWhen.test(mob, stalkTarget);
    }

    private Optional<LivingEntity> getStalkTarget(E mob) {
        return HunterAi.getHuntTarget(mob);
    }

    private boolean canCrouch(E mob) {
        LivingEntity stalkTarget = this.getStalkTarget(mob).orElse(null);
        return this.isValidStalkTarget(mob, stalkTarget);
    }

    private boolean canPounce(E mob){
        boolean canPounce = false;
        if(mob.hasPose(Pose.CROUCHING)){
            LivingEntity stalkTarget = this.getStalkTarget(mob).orElse(null);
            if (this.isValidStalkTarget(mob, stalkTarget)) {
                canPounce = AiUtil.isPathClear(mob, stalkTarget, this.pounceDistance, this.pounceHeight);
            }
        }
        if(!canPounce){
            AiUtil.resetPose(mob, Pose.CROUCHING);
            //this.clearTarget(mob);
        }
        return canPounce;
    }

    private boolean isMidPounce(E mob){
        double yD = mob.getDeltaMovement().y;
        return (Mth.square(yD) >= MIN_Y_DELTA || !mob.onGround());
    }

    @Override
    protected void tick(ServerLevel level, E mob, long gameTime) {
        this.getStalkTarget(mob).ifPresent(st -> BehaviorUtils.lookAtEntity(mob, st));
        switch (this.state){
            case STALK -> {
                LivingEntity stalkTarget = this.getStalkTarget(mob).get();
                if (mob.distanceToSqr(stalkTarget) <= Mth.square(this.pounceDistance)) {
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
                    this.state = State.POUNCE;
                }
            }
            case POUNCE -> {
                LivingEntity stalkTarget = this.getStalkTarget(mob).get();
                GenericAi.stopWalking(mob);
                BehaviorUtils.lookAtEntity(mob, stalkTarget);
                mob.setPose(Pose.LONG_JUMPING);
                LongJumpAi.setMidJump(mob);
                Vec3 jumpVector = stalkTarget.position().subtract(mob.position()).normalize();
                double xzDScale = this.pounceDistance * 2.0D / 15.0D; // 0.8D for 6
                double yD = this.pounceHeight * 3.0D / 10.0D; // 0.9D for 3
                mob.setDeltaMovement(mob.getDeltaMovement().add(jumpVector.x * xzDScale, yD, jumpVector.z * xzDScale));
                this.state = State.MID_POUNCE;
            }
        }
    }

    private void setWalkAndLookTarget(E mob, LivingEntity target) {
        AiUtil.setWalkAndLookTargetMemories(mob, target, this.speedModifier, this.pounceDistance - 1);
    }

    @Override
    protected void stop(ServerLevel level, E mob, long gameTime) {
        if(this.state == State.CROUCH_ANIMATION){
            AiUtil.resetPose(mob, Pose.CROUCHING);
        }
        if(this.state == State.MID_POUNCE){
            LongJumpAi.clearMidJump(mob);
            AiUtil.resetPose(mob, Pose.LONG_JUMPING);
        }
        this.state = State.DONE;
        this.clearTarget(mob);
    }

    enum State {
        STALK,
        CROUCH_ANIMATION,
        POUNCE,
        MID_POUNCE,
        DONE
    }

}
