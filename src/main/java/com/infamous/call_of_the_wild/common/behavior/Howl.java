package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.entity.dog.SharedWolfAi;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings({"NullableProblems", "unused"})
public class Howl<E extends LivingEntity> extends Behavior<E> {
    private final Predicate<E> wantsToHowl;
    private final BiPredicate<E, LivingEntity> wantsToListen;
    private final Consumer<E> onHowlStarted;
    private final int radiusXZ;
    private final int radiusY;
    private final Function<LivingEntity, Float> alertableSpeedModifier;
    private final int closeEnough;
    private final UniformInt howlCooldown;

    public Howl(BiPredicate<E, LivingEntity> canAlert, Consumer<E> onHowlStarted, Function<LivingEntity, Float> alertableSpeedModifier, int closeEnough, UniformInt howlCooldown) {
        this(le -> true, canAlert, onHowlStarted, 32, 16, alertableSpeedModifier, closeEnough, howlCooldown);
    }

    public Howl(Predicate<E> wantsToHowl, BiPredicate<E, LivingEntity> canAlert, Consumer<E> onHowlStarted, Function<LivingEntity, Float> alertableSpeedModifier, int closeEnough, UniformInt howlCooldown) {
        this(wantsToHowl, canAlert, onHowlStarted, 32, 16, alertableSpeedModifier, closeEnough, howlCooldown);
    }

    public Howl(Predicate<E> wantsToHowl, BiPredicate<E, LivingEntity> canAlert, Consumer<E> onHowlStarted, int radiusXZ, int radiusY, Function<LivingEntity, Float> alertableSpeedModifier, int closeEnough, UniformInt howlCooldown) {
        super(ImmutableMap.of(
                COTWMemoryModuleTypes.NEAREST_ADULTS.get(), MemoryStatus.VALUE_ABSENT,
                COTWMemoryModuleTypes.NEAREST_ALLIES.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.HOWLED_RECENTLY.get(), MemoryStatus.VALUE_ABSENT
        ));
        this.wantsToHowl = wantsToHowl;
        this.wantsToListen = canAlert;
        this.onHowlStarted = onHowlStarted;
        this.radiusXZ = radiusXZ;
        this.radiusY = radiusY;
        this.alertableSpeedModifier = alertableSpeedModifier;
        this.closeEnough = closeEnough;
        this.howlCooldown = howlCooldown;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
        return this.wantsToHowl.test(mob);
    }

    @Override
    protected void start(ServerLevel level, E mob, long gameTime) {
        AABB searchBox = mob.getBoundingBox().inflate(this.radiusXZ, this.radiusY, this.radiusXZ);
        List<LivingEntity> listeners = level.getEntitiesOfClass(LivingEntity.class, searchBox, (le) -> this.wantsToListen(mob, le));
        listeners.forEach(le -> {
            BehaviorUtils.setWalkAndLookTargetMemories(le, mob, this.alertableSpeedModifier.apply(le), this.closeEnough);
            this.scheduleSignal(level, mob.position(), new EntityPositionSource(le, le.getEyeHeight()));
        });
        this.onHowlStarted.accept(mob);
        int howlCooldownInTicks = this.howlCooldown.sample(mob.getRandom());
        SharedWolfAi.setHowledRecently(mob, howlCooldownInTicks);
        GenericAi.getNearbyAllies(mob).forEach(le -> SharedWolfAi.setHowledRecently(le, howlCooldownInTicks));
    }

    private boolean wantsToListen(E mob, LivingEntity other) {
        return other != mob
                && other.isAlive()
                && this.wantsToListen.test(mob, other);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void scheduleSignal(ServerLevel level, Vec3 eventSource, PositionSource listenerSource) {
        Vec3 distanceVec = listenerSource.getPosition(level).get().subtract(eventSource);
        int particleCount = Mth.floor(distanceVec.length()) + 7;

        listenerSource.getPosition(level).ifPresent(position -> {
            float receivingDistance = (float)eventSource.distanceTo(position);
            int travelTimeInTicks = Mth.floor(receivingDistance);
            level.sendParticles(new VibrationParticleOption(listenerSource, travelTimeInTicks), eventSource.x, eventSource.y, eventSource.z, particleCount, 0.0D, 0.0D, 0.0D, 0.0D);
        });
    }
}
