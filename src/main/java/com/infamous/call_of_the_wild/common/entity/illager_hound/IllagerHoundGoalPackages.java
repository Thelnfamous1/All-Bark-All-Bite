package com.infamous.call_of_the_wild.common.entity.illager_hound;

import com.google.common.collect.ImmutableList;
import com.infamous.call_of_the_wild.common.ABABTags;
import com.infamous.call_of_the_wild.common.ai.AiUtil;
import com.infamous.call_of_the_wild.common.behavior.HurtByEntityTrigger;
import com.infamous.call_of_the_wild.common.behavior.LeapAtTarget;
import com.infamous.call_of_the_wild.common.behavior.Sprint;
import com.infamous.call_of_the_wild.common.behavior.pet.FollowOwner;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtByTarget;
import com.infamous.call_of_the_wild.common.behavior.pet.OwnerHurtTarget;
import com.infamous.call_of_the_wild.common.entity.SharedWolfAi;
import com.infamous.call_of_the_wild.common.ai.BrainUtil;
import com.infamous.call_of_the_wild.common.ai.GenericAi;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class IllagerHoundGoalPackages {

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super IllagerHound>>> getCorePackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Swim(SharedWolfAi.JUMP_CHANCE_IN_WATER),
                        new HurtByEntityTrigger<>(IllagerHoundGoalPackages::onHurtBy),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new OwnerHurtByTarget<>(),
                        new OwnerHurtTarget<>()
                ));
    }

    private static void onHurtBy(IllagerHound victim, LivingEntity attacker){
        retaliate(victim, attacker);
        GenericAi.getNearbyAllies(victim).forEach(nearbyAlly -> retaliate(nearbyAlly, attacker));
    }

    private static void retaliate(IllagerHound victim, LivingEntity attacker) {
        if (victim.canAttack(attacker) && !BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(victim, attacker, SharedWolfAi.TOO_FAR_TO_SWITCH_TARGETS)) {
            StartAttacking.setAttackTarget(victim, attacker);
        }
    }

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super IllagerHound>>> getFightPackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(),
                        new SetWalkTargetFromAttackTargetIfTargetOutOfReach(SharedWolfAi.SPEED_MODIFIER_CHASING),
                        new LeapAtTarget(SharedWolfAi.LEAP_YD, SharedWolfAi.TOO_CLOSE_TO_LEAP, SharedWolfAi.TOO_FAR_TO_LEAP, SharedWolfAi.LEAP_COOLDOWN),
                        new MeleeAttack(SharedWolfAi.ATTACK_COOLDOWN_TICKS),
                        new StopAttackingIfTargetInvalid<>()
                ));
    }

    static ImmutableList<? extends Pair<Integer, ? extends Behavior<? super IllagerHound>>> getIdlePackage() {
        return BrainUtil.createPriorityPairs(0,
                ImmutableList.of(
                        new Sprint<>(SharedWolfAi.TOO_FAR_FROM_WALK_TARGET),
                        new FollowOwner<>(AiUtil::getOwner, SharedWolfAi.SPEED_MODIFIER_WALKING, SharedWolfAi.CLOSE_ENOUGH_TO_OWNER, SharedWolfAi.TOO_FAR_FROM_OWNER),
                        new StartAttacking<>(IllagerHoundGoalPackages::findNearestValidAttackTarget),
                        createIdleLookBehavior(),
                        createIdleMovementBehaviors()
                ));
    }

    private static RunSometimes<IllagerHound> createIdleLookBehavior() {
        return new RunSometimes<>(new SetEntityLookTarget(SharedWolfAi.MAX_LOOK_DIST), UniformInt.of(30, 60));
    }

    private static RunOne<IllagerHound> createIdleMovementBehaviors() {
        return new RunOne<>(
                ImmutableList.of(
                        Pair.of(new RandomStroll(SharedWolfAi.SPEED_MODIFIER_WALKING), 2),
                        Pair.of(new SetWalkTargetFromLookTarget(SharedWolfAi.SPEED_MODIFIER_WALKING, 3), 2),
                        Pair.of(new DoNothing(30, 60), 1)
                )
        );
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(IllagerHound illagerHound) {
        Optional<Player> nearestAttackablePlayer = illagerHound.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
        return nearestAttackablePlayer.isPresent() ? nearestAttackablePlayer : GenericAi.getNearestVisibleLivingEntities(illagerHound).findClosest(le -> isTargetable(illagerHound, le));
    }

    private static boolean isTargetable(IllagerHound illagerHound, LivingEntity target) {
        EntityType<?> type = target.getType();
        return type.is(ABABTags.ILLAGER_HOUND_ALWAYS_HOSTILES) && Sensor.isEntityAttackable(illagerHound, target);
    }
}
