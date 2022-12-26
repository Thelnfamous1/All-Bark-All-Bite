package com.infamous.call_of_the_wild.common.behavior;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings({"NullableProblems", "unused"})
public class Howl<E extends LivingEntity> extends Behavior<E> {
    private final Predicate<E> wantsToHowl;
    private final BiPredicate<E, LivingEntity> canAlert;
    private final Consumer<E> onHowlStarted;
    private final int radiusXZ;
    private final int radiusY;
    private final float speedModifier;
    private final int closeEnough;
    private final int howlCooldown;

    public Howl(Predicate<E> wantsToHowl, BiPredicate<E, LivingEntity> canAlert, Consumer<E> onHowlStarted, float speedModifier, int closeEnough, int howlCooldown) {
        this(wantsToHowl, canAlert, onHowlStarted, 32, 16, speedModifier, closeEnough, howlCooldown);
    }

    public Howl(Predicate<E> wantsToHowl, BiPredicate<E, LivingEntity> canAlert, Consumer<E> onHowlStarted, int radiusXZ, int radiusY, float speedModifier, int closeEnough, int howlCooldown) {
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
        this.speedModifier = speedModifier;
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
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, searchBox, (le) -> this.isAlertable(mob, le));
        list.forEach(le -> BehaviorUtils.setWalkAndLookTargetMemories(le, mob, this.speedModifier, this.closeEnough));
        this.onHowlStarted.accept(mob);
        this.setHowledRecently(mob);
        GenericAi.getNearbyKin(mob).forEach(this::setHowledRecently);
    }

    private void setHowledRecently(LivingEntity mob) {
        mob.getBrain().setMemoryWithExpiry(COTWMemoryModuleTypes.HOWLED_RECENTLY.get(), true, this.howlCooldown);
    }

    private boolean isAlertable(E mob, LivingEntity other) {
        return other != mob
                && other.isAlive()
                && this.canAlert.test(mob, other);
    }
}
