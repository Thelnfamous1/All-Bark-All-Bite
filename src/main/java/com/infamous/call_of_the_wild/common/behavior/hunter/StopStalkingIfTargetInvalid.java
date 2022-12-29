package com.infamous.call_of_the_wild.common.behavior.hunter;

import com.google.common.collect.ImmutableMap;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import com.infamous.call_of_the_wild.common.util.HunterAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@SuppressWarnings("NullableProblems")
public class StopStalkingIfTargetInvalid<E extends Mob> extends Behavior<E> {
   private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;
   private final Predicate<LivingEntity> stopStalkingWhen;
   private final BiConsumer<E, LivingEntity> onTargetErased;
   private final boolean canGrowTiredOfTryingToReachTarget;

   public StopStalkingIfTargetInvalid(Predicate<LivingEntity> stopStalkingWhen, BiConsumer<E, LivingEntity> onTargetErased, boolean canGrowTiredOfTryingToReachTarget) {
      super(ImmutableMap.of(
              COTWMemoryModuleTypes.STALK_TARGET.get(), MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED));
      this.stopStalkingWhen = stopStalkingWhen;
      this.onTargetErased = onTargetErased;
      this.canGrowTiredOfTryingToReachTarget = canGrowTiredOfTryingToReachTarget;
   }

   public StopStalkingIfTargetInvalid(Predicate<LivingEntity> stopStalkingWhen, BiConsumer<E, LivingEntity> onTargetErased) {
      this(stopStalkingWhen, onTargetErased, true);
   }

   @SuppressWarnings("unused")
   public StopStalkingIfTargetInvalid(Predicate<LivingEntity> stopStalkingWhen) {
      this(stopStalkingWhen, (m, le) -> {});
   }

   public StopStalkingIfTargetInvalid(BiConsumer<E, LivingEntity> onTargetErased) {
      this((le) -> false, onTargetErased);
   }

   @SuppressWarnings("unused")
   public StopStalkingIfTargetInvalid() {
      this((le) -> false, (m, le) -> {});
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      LivingEntity target = this.getStalkTarget(mob).get();
      if (!mob.canAttack(target)) {
         this.stopStalking(mob, target);
      } else if (this.canGrowTiredOfTryingToReachTarget && isTiredOfTryingToReachTarget(mob)) {
         this.stopStalking(mob, target);
      } else if (!target.isAlive()) {
         this.stopStalking(mob, target);
      } else if (target.level != mob.level) {
         this.stopStalking(mob, target);
      } else if (this.stopStalkingWhen.test(target)) {
         this.stopStalking(mob, target);
      }
   }

   private Optional<LivingEntity> getStalkTarget(E mob) {
      return HunterAi.getStalkTarget(mob);
   }

   private static <E extends LivingEntity> boolean isTiredOfTryingToReachTarget(E mob) {
      Optional<Long> optional = mob.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
      return optional.isPresent() && mob.level.getGameTime() - optional.get() > TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE;
   }

   protected void stopStalking(E mob, LivingEntity target) {
      this.onTargetErased.accept(mob, target);
      mob.getBrain().eraseMemory(COTWMemoryModuleTypes.STALK_TARGET.get());
   }
}