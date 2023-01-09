package com.infamous.call_of_the_wild.common.behavior.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("NullableProblems")
public class GiveItemToTarget<E extends LivingEntity> extends Behavior<E> {
   private final Function<E, ItemStack> itemGetter;
   private final Function<E, Optional<LivingEntity>> targetGetter;
   private final int closeEnough;
   private final Consumer<E> onThrown;
   private final int throwYOffset;

   public GiveItemToTarget(Function<E, ItemStack> itemGetter, Function<E, Optional<LivingEntity>> targetGetter, int closeEnough, Consumer<E> onThrown){
      this(itemGetter, targetGetter, closeEnough, 0, onThrown);
   }

   public GiveItemToTarget(Function<E, ItemStack> itemGetter, Function<E, Optional<LivingEntity>> targetGetter, int closeEnough, int throwYOffset, Consumer<E> onThrown) {
      super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED));
      this.itemGetter = itemGetter;
      this.targetGetter = targetGetter;
      this.closeEnough = closeEnough;
      this.throwYOffset = throwYOffset;
      this.onThrown = onThrown;
   }

   @Override
   protected boolean checkExtraStartConditions(ServerLevel level, E mob) {
      if (this.itemGetter.apply(mob).isEmpty()) {
         return false;
      } else {
         Optional<LivingEntity> target = this.targetGetter.apply(mob);
         if(target.isEmpty() || target.get().isSpectator()){
            return false;
         } else{
            return mob.distanceToSqr(target.get()) <= Mth.square(this.closeEnough);
         }
      }
   }

   @SuppressWarnings("OptionalGetWithoutIsPresent")
   @Override
   protected void start(ServerLevel level, E mob, long gameTime) {
      ItemStack toThrow = this.itemGetter.apply(mob).split(1);
      if (!toThrow.isEmpty()) {
         BehaviorUtils.throwItem(mob, toThrow, this.getThrowPosition(this.targetGetter.apply(mob).get()));
         this.onThrown.accept(mob);
      }
   }

   private Vec3 getThrowPosition(LivingEntity target) {
      return target.position().add(0.0D, this.throwYOffset, 0.0D);
   }
}