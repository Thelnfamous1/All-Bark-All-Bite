package com.infamous.call_of_the_wild.common.behavior.long_jump;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.ABABMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.ai.LongJumpAi;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class LongJumpToTarget<E extends Mob> extends Behavior<E> {
   private static final int TIME_OUT_DURATION = 200;
   private final UniformInt timeBetweenLongJumps;
   protected final float maxJumpVelocity;
   @Nullable
   protected Vec3 initialPosition;
   @Nullable
   protected Vec3 chosenJump;
   protected long prepareJumpStart;
   private final Function<E, SoundEvent> getJumpSound;
   private final int prepareJumpDuration;
   private final Predicate<BlockState> acceptableLandingSpot;

   public LongJumpToTarget(UniformInt timeBetweenLongJumps, float maxJumpVelocity, Function<E, SoundEvent> getJumpSound, int prepareJumpDuration) {
      this(timeBetweenLongJumps, maxJumpVelocity, getJumpSound, prepareJumpDuration, (bs) -> false);
   }

   public LongJumpToTarget(UniformInt timeBetweenLongJumps, float maxJumpVelocity, Function<E, SoundEvent> getJumpSound, int prepareJumpDuration, Predicate<BlockState> acceptableLandingSpot) {
      super(ImmutableMap.of(
                      ABABMemoryModuleTypes.LONG_JUMP_TARGET.get(), MemoryStatus.VALUE_PRESENT,
                      MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                      MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT,
                      MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_ABSENT),
              TIME_OUT_DURATION);
      this.timeBetweenLongJumps = timeBetweenLongJumps;
      this.maxJumpVelocity = maxJumpVelocity;
      this.getJumpSound = getJumpSound;
      this.prepareJumpDuration = prepareJumpDuration;
      this.acceptableLandingSpot = acceptableLandingSpot;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      boolean canStart = mob.isOnGround()
              && !mob.isInWater()
              && !mob.isInLava()
              && !level.getBlockState(mob.blockPosition()).is(Blocks.HONEY_BLOCK)
              && this.hasValidJumpTarget(mob);
      if (!canStart) {
         LongJumpAi.setLongJumpCooldown(mob, this.timeBetweenLongJumps.sample(level.random) / 2);
         LongJumpAi.clearLongJumpTarget(mob);
      }

      return canStart;
   }

   private boolean hasValidJumpTarget(LivingEntity mob) {
      BlockPos initialPosition = mob.blockPosition();
      return this.getLongJumpTarget(mob)
              .filter(target -> !target.currentBlockPosition().equals(initialPosition))
              .isPresent();
   }

   private Optional<PositionTracker> getLongJumpTarget(LivingEntity mob) {
      return LongJumpAi.getLongJumpTarget(mob);
   }

   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      this.chosenJump = null;
      this.pickCandidate(level, mob, gameTime);
      this.initialPosition = mob.position();
   }

   protected void pickCandidate(ServerLevel level, E mob, long gameTime) {
      Optional<PositionTracker> longJumpTarget = this.getLongJumpTarget(mob);
      longJumpTarget.ifPresent(target -> {
         BlockPos targetBlockPosition = target.currentBlockPosition();
         if (!this.isAcceptableLandingPosition(level, mob, targetBlockPosition)) {
            return;
         }

         Vec3 optimalJumpVector = LongJumpAi.calculateOptimalJumpVector(mob, target.currentPosition(), this.maxJumpVelocity, LongJumpAi.ALLOWED_ANGLES);
         if (optimalJumpVector == null) {
            return;
         }

         mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, target);
         this.chosenJump = optimalJumpVector;
         this.prepareJumpStart = gameTime;
      });
   }

   protected boolean isAcceptableLandingPosition(ServerLevel level, E mob, BlockPos targetPosition) {
      BlockPos currentPosition = mob.blockPosition();
      int x = currentPosition.getX();
      int z = currentPosition.getZ();
      if (x == targetPosition.getX() && z == targetPosition.getZ()) {
         return false;
      } else if (!mob.getNavigation().isStableDestination(targetPosition) && !this.acceptableLandingSpot.test(level.getBlockState(targetPosition.below()))) {
         return false;
      } else {
         return mob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(mob.level, targetPosition.mutable())) == 0.0F;
      }
   }

   @Override
   protected boolean canStillUse(ServerLevel level, E mob, long gameTime) {
      boolean canStillUse = this.initialPosition != null
              && this.initialPosition.equals(mob.position())
              && !mob.isInWaterOrBubble()
              && (this.chosenJump != null || this.hasValidJumpTarget(mob));
      if (!canStillUse && !LongJumpAi.isMidJump(mob)) {
         LongJumpAi.setLongJumpCooldown(mob, this.timeBetweenLongJumps.sample(level.random) / 2);
         mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
         LongJumpAi.clearLongJumpTarget(mob);
      }
      return canStillUse;
   }

   @Override
   protected void tick(ServerLevel level, E mob, long gameTime) {
      if (this.chosenJump != null) {
         if (gameTime - this.prepareJumpStart >= this.prepareJumpDuration) {
            mob.setYRot(mob.yBodyRot);
            mob.setDiscardFriction(true);
            double jumpLength = this.chosenJump.length();
            double boostedJumpLength = jumpLength + mob.getJumpBoostPower();
            mob.setDeltaMovement(this.chosenJump.scale(boostedJumpLength / jumpLength));
            mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
            level.playSound(null, mob, this.getJumpSound.apply(mob), SoundSource.NEUTRAL, 1.0F, 1.0F);
         }
      }
   }
}