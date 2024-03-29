package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import com.infamous.all_bark_all_bite.common.registry.ABABActivities;
import com.infamous.all_bark_all_bite.common.util.VectorTracker;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class PerchAndSearch<E extends PathfinderMob> extends Behavior<E> {
    private static final float START_CHANCE = 0.02F;
    private final Predicate<E> canPerch;
    private final BiConsumer<E, Boolean> toggleSitting;
    private int lookTime;
    private int looksRemaining;

    public PerchAndSearch(Predicate<E> canPerch, BiConsumer<E, Boolean> toggleSitting) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED

        ));
        this.canPerch = canPerch;
        this.toggleSitting = toggleSitting;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        return mob.getLastHurtByMob() == null
                && mob.getRandom().nextFloat() <= START_CHANCE
                && mob.getNavigation().isDone()
                && this.canPerch.test(mob);
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
        this.lookTime = 80 + random.nextInt(20);
        Vec3 lookAtPos = new Vec3(mob.getX() + relX, mob.getEyeY(), mob.getZ() + relZ);
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new VectorTracker(lookAtPos));
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
        return this.looksRemaining > 0 && GenericAi.getWalkTarget(mob).isEmpty();
    }

    @Override
    protected void stop(ServerLevel level, E mob, long gameTime) {
        if(!mob.getBrain().isActive(ABABActivities.SIT.get())){
            this.toggleSitting.accept(mob, false);
        }
    }
}
