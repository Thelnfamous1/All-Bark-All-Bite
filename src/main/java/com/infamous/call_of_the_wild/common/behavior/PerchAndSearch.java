package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.AiUtil;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import com.infamous.call_of_the_wild.common.util.PositionTrackerImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@SuppressWarnings({"NullableProblems", "unused"})
public class PerchAndSearch<E extends PathfinderMob> extends Behavior<E> {
    private final Predicate<E> isSitting;
    private final BiConsumer<E, Boolean> toggleSitting;
    private int lookTime;
    private int looksRemaining;

    public PerchAndSearch(Predicate<E> isSitting, BiConsumer<E, Boolean> toggleSitting) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.IS_ALERT.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ));
        this.isSitting = isSitting;
        this.toggleSitting = toggleSitting;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        return !mob.isSleeping()
                && mob.getNavigation().isDone()
                && !mob.hasPose(Pose.CROUCHING)
                && !this.isSitting.test(mob);
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        this.resetLook(mob);
        this.looksRemaining = 2 + mob.getRandom().nextInt(3);
        this.toggleSitting.accept(mob, true);
        GenericAi.stopWalking(mob);
    }

    private void resetLook(E mob) {
        RandomSource random = mob.getRandom();
        double randomLookAngle = (Math.PI * 2D) * random.nextDouble();
        double relX = Math.cos(randomLookAngle);
        double relZ = Math.sin(randomLookAngle);
        this.lookTime = AiUtil.reducedTickDelay(80 + random.nextInt(20));
        Vec3 lookAtPos = new Vec3(mob.getX() + relX, mob.getEyeY(), mob.getZ() + relZ);
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new PositionTrackerImpl(lookAtPos));
    }

    @Override
    protected void tick(ServerLevel level, E mob, long gameTime) {
        --this.lookTime;
        if (this.lookTime <= 0) {
            --this.looksRemaining;
            this.resetLook(mob);
        }
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E mob, long gameTime) {
        return this.looksRemaining > 0;
    }

    @Override
    protected void stop(ServerLevel level, E mob, long gameTime) {
        this.toggleSitting.accept(mob, false);
    }
}
