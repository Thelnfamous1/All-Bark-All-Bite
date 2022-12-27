package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.entity.dog.WolflikeAi;
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
    private final BiPredicate<E, LivingEntity> canAlert;
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
                COTWMemoryModuleTypes.NEARBY_ADULTS.get(), MemoryStatus.VALUE_ABSENT,
                COTWMemoryModuleTypes.NEARBY_KIN.get(), MemoryStatus.REGISTERED,
                COTWMemoryModuleTypes.HOWLED_RECENTLY.get(), MemoryStatus.VALUE_ABSENT
        ));
        this.wantsToHowl = wantsToHowl;
        this.canAlert = canAlert;
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
        List<LivingEntity> alertables = level.getEntitiesOfClass(LivingEntity.class, searchBox, (le) -> this.isAlertable(mob, le));
        alertables.forEach(le -> {
            BehaviorUtils.setWalkAndLookTargetMemories(le, mob, this.alertableSpeedModifier.apply(le), this.closeEnough);
            this.visualizeAlert(level, mob.position(), new EntityPositionSource(le, le.getEyeHeight()));
        });
        this.onHowlStarted.accept(mob);
        int howlCooldownInTicks = this.howlCooldown.sample(mob.getRandom());
        WolflikeAi.setHowledRecently(mob, howlCooldownInTicks);
        GenericAi.getNearbyKin(mob).forEach(le -> WolflikeAi.setHowledRecently(le, howlCooldownInTicks));
    }

    private boolean isAlertable(E mob, LivingEntity other) {
        return other != mob
                && other.isAlive()
                && this.canAlert.test(mob, other);
    }

    private void visualizeAlert(ServerLevel level, Vec3 eventSource, PositionSource listenerSource) {
        listenerSource.getPosition(level).ifPresent(position -> {
            float receivingDistance = (float)eventSource.distanceTo(position);
            int travelTimeInTicks = Mth.floor(receivingDistance);
            level.sendParticles(new VibrationParticleOption(listenerSource, travelTimeInTicks), eventSource.x, eventSource.y, eventSource.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        });
    }
}
