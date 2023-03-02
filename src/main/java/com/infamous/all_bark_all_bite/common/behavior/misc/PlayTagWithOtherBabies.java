package com.infamous.all_bark_all_bite.common.behavior.misc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.infamous.all_bark_all_bite.common.registry.ABABMemoryModuleTypes;
import com.infamous.all_bark_all_bite.common.util.MiscUtil;
import com.infamous.all_bark_all_bite.common.util.ai.AiUtil;
import com.infamous.all_bark_all_bite.common.util.ai.GenericAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

public class PlayTagWithOtherBabies extends Behavior<PathfinderMob> {
   private static final int MAX_FLEE_XZ_DIST = 20;
   private static final int MAX_FLEE_Y_DIST = 8;
   private final BiPredicate<ServerLevel, Vec3> validFleePos;
   private final float fleeSpeedModifier;
   private final float chaseSpeedModifier;
   private static final int MAX_CHASERS_PER_TARGET = 5;
   private static final int AVERAGE_WAIT_TIME_BETWEEN_RUNS = 10;

   public PlayTagWithOtherBabies(float fleeSpeedModifier, float chaseSpeedModifier){
      this((level, vec3) -> true, fleeSpeedModifier, chaseSpeedModifier);
   }

   public PlayTagWithOtherBabies(BiPredicate<ServerLevel, Vec3> validFleePos, float fleeSpeedModifier, float chaseSpeedModifier) {
      super(ImmutableMap.of(
              ABABMemoryModuleTypes.NEAREST_VISIBLE_BABIES.get(), MemoryStatus.VALUE_PRESENT,
              MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
              MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
              MemoryModuleType.INTERACTION_TARGET, MemoryStatus.REGISTERED));
      this.validFleePos = validFleePos;
      this.fleeSpeedModifier = fleeSpeedModifier;
      this.chaseSpeedModifier = chaseSpeedModifier;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, PathfinderMob mob) {
      return mob.isBaby() && MiscUtil.oneInChance(mob.getRandom(), AVERAGE_WAIT_TIME_BETWEEN_RUNS) && this.hasFriendsNearby(mob);
   }

   @Override
   protected void start(ServerLevel level, PathfinderMob mob, long gameTime) {
      LivingEntity chasingMe = this.seeIfSomeoneIsChasingMe(mob);
      if (chasingMe != null) {
         this.fleeFromChaser(level, mob);
      } else {
         Optional<LivingEntity> someoneBeingChased = this.findSomeoneBeingChased(mob);
         if (someoneBeingChased.isPresent()) {
            this.chaseKid(mob, someoneBeingChased.get());
         } else {
            this.findSomeoneToChase(mob).ifPresent((kid) -> this.chaseKid(mob, kid));
         }
      }
   }

   private void fleeFromChaser(ServerLevel level, PathfinderMob mob) {
      for(int i = 0; i < 10; ++i) {
         Vec3 fleePos = LandRandomPos.getPos(mob, MAX_FLEE_XZ_DIST, MAX_FLEE_Y_DIST);
         if (fleePos != null && this.validFleePos.test(level, fleePos)) {
            mob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(fleePos, this.fleeSpeedModifier, 0));
            return;
         }
      }

   }

   private void chaseKid(PathfinderMob mob, LivingEntity kid) {
      Brain<?> brain = mob.getBrain();
      brain.setMemory(MemoryModuleType.INTERACTION_TARGET, kid);
      AiUtil.setWalkAndLookTargetMemories(mob, kid, this.chaseSpeedModifier, 1);
   }

   private Optional<LivingEntity> findSomeoneToChase(PathfinderMob mob) {
      return this.getFriendsNearby(mob).stream().findAny();
   }

   private Optional<LivingEntity> findSomeoneBeingChased(PathfinderMob mob) {
      Map<LivingEntity, Integer> friendsToChasers = this.checkHowManyChasersEachFriendHas(mob);
      //noinspection ConstantConditions
      return friendsToChasers.entrySet()
              .stream()
              .sorted(Comparator.comparingInt(Map.Entry::getValue))
              .filter((entry) -> entry.getValue() > 0 && entry.getValue() <= MAX_CHASERS_PER_TARGET)
              .map(Map.Entry::getKey)
              .findFirst();
   }

   private Map<LivingEntity, Integer> checkHowManyChasersEachFriendHas(PathfinderMob mob) {
      Map<LivingEntity, Integer> map = Maps.newHashMap();
      this.getFriendsNearby(mob)
              .stream()
              .filter(this::isChasingSomeone)
              .forEach((chaser) -> map.compute(this.whoAreYouChasing(chaser), (friend, chasers) -> chasers == null ? 1 : chasers + 1));
      return map;
   }

   private List<LivingEntity> getFriendsNearby(PathfinderMob mob) {
      return GenericAi.getNearestVisibleBabies(mob);
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   private LivingEntity whoAreYouChasing(LivingEntity mob) {
      return mob.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
   }

   @Nullable
   private LivingEntity seeIfSomeoneIsChasingMe(LivingEntity mob) {
      return GenericAi.getNearestVisibleBabies(mob).stream().filter((baby) -> this.isFriendChasingMe(mob, baby)).findAny().orElse(null);
   }

   private boolean isChasingSomeone(LivingEntity mob) {
      return mob.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
   }

   private boolean isFriendChasingMe(LivingEntity mob, LivingEntity friend) {
      return friend.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).filter((target) -> target == mob).isPresent();
   }

   private boolean hasFriendsNearby(PathfinderMob mob) {
      return !GenericAi.getNearestVisibleBabies(mob).isEmpty();
   }
}