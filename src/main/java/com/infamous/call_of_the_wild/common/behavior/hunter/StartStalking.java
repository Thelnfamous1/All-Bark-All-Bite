package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.HunterAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

@SuppressWarnings("NullableProblems")
public class StartStalking<E extends PathfinderMob> extends Behavior<E> {
    private final BiPredicate<E, LivingEntity> wantsToStalk;
    private final int tooClose;
    private final Function<E, Boolean> isInterested;

    public StartStalking(BiPredicate<E, LivingEntity> wantsToStalk, int tooClose, Function<E, Boolean> isInterested) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.STALK_TARGET.get(), MemoryStatus.VALUE_ABSENT,
                COTWMemoryModuleTypes.NEAREST_VISIBLE_HUNTABLE.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.VALUE_ABSENT
        ));
        this.wantsToStalk = wantsToStalk;
        this.tooClose = tooClose;
        this.isInterested = isInterested;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        if (mob.isSleeping()) {
            return false;
        } else {
            LivingEntity target = this.getNearestVisibleHuntable(mob).get();
            return target.isAlive()
                    && this.wantsToStalk.test(mob, target)
                    && !mob.closerThan(target, this.tooClose)
                    && this.isNotStalking(mob);
                    //&& !AiUtil.isJumping(mob);
        }
    }

    private boolean isNotStalking(E mob) {
        return !mob.hasPose(Pose.CROUCHING) && !this.isInterested.apply(mob);
    }

    @NotNull
    private Optional<LivingEntity> getNearestVisibleHuntable(E mob) {
        return HunterAi.getNearestVisibleHuntable(mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        mob.getBrain().setMemory(COTWMemoryModuleTypes.STALK_TARGET.get(), this.getNearestVisibleHuntable(mob).get());
        mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }

}
