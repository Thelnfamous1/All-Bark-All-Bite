package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.entity.dog.ai.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.BiConsumer;

@SuppressWarnings("NullableProblems")
public class Pounce<E extends PathfinderMob> extends Behavior<E> {
    private final BiConsumer<E, Boolean> toggleInterest;
    private final double maxJumpVelocity;

    public Pounce(BiConsumer<E, Boolean> toggleInterest, double maxJumpVelocity) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.STALK_TARGET.get(), MemoryStatus.VALUE_PRESENT,
                COTWMemoryModuleTypes.POUNCE_DELAY.get(), MemoryStatus.VALUE_ABSENT,
                COTWMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
        this.toggleInterest = toggleInterest;
        this.maxJumpVelocity = maxJumpVelocity;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (!this.isReadyToPounce(mob)) {
            return false;
        } else{
            LivingEntity target = this.getStalkTarget(mob).get();
            if(!target.isAlive()){
                return false;
            }

            if (target.getMotionDirection() != target.getDirection()) {
                return false;
            } else {
                Vec3 optimalJumpVector = LongJumpAi.calculateOptimalJumpVector(mob, target.position(), this.maxJumpVelocity, LongJumpAi.ALLOWED_ANGLES);
                boolean canPounce = optimalJumpVector != null;
                if (!canPounce) {
                    //mob.getNavigation().createPath(target, 0);
                    this.setNotReadyToPounce(mob);
                }

                return canPounce;
            }
        }
    }

    private boolean isReadyToPounce(E mob) {
        return mob.hasPose(Pose.CROUCHING);
    }

    private void setNotReadyToPounce(E mob) {
        this.toggleInterest.accept(mob, false);
        if(mob.hasPose(Pose.CROUCHING)){
            mob.setPose(Pose.STANDING);
        }
    }

    private Optional<LivingEntity> getStalkTarget(E mob) {
        return HunterAi.getStalkTarget(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        //mob.setJumping(true);
        this.setNotReadyToPounce(mob);
        LivingEntity target = this.getStalkTarget(mob).get();

        AngerAi.setAngerTarget(mob, target, SharedWolfAi.ANGER_DURATION.sample(mob.getRandom()));

        mob.getBrain().eraseMemory(COTWMemoryModuleTypes.STALK_TARGET.get());
        LongJumpAi.setLongJumpTarget(mob, new AlwaysVisibleEntityTracker(target, false));
        AiUtil.alwaysLookAtEntity(mob, target, true);
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }
}
