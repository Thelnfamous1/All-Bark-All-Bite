package com.infamous.call_of_the_wild.common.util;

import com.google.common.collect.Lists;
import com.infamous.call_of_the_wild.common.registry.COTWMemoryModuleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LongJumpAi {
    public static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList(65, 70, 75, 80);

    @Nullable
    public static Vec3 calculateOptimalJumpVector(Mob mob, Vec3 targetPos, double maxJumpVelocity, List<Integer> allowedAnglesIn) {
       List<Integer> allowedAngles = Lists.newArrayList(allowedAnglesIn);
       Collections.shuffle(allowedAngles);

       for(int allowedAngle : allowedAngles) {
          Vec3 jumpVectorForAngle = calculateJumpVectorForAngle(mob, targetPos, allowedAngle, maxJumpVelocity);
          if (jumpVectorForAngle != null) {
             return jumpVectorForAngle;
          }
       }

       return null;
    }

    @Nullable
    public static Vec3 calculateJumpVectorForAngle(Mob mob, Vec3 targetPos, int angle, double maxJumpVelocity) {
       Vec3 startPos = mob.position();
       Vec3 horizontalDiff = targetPos.subtract(startPos.x, targetPos.y, startPos.z).normalize().scale(0.5D);
       targetPos = targetPos.subtract(horizontalDiff);
       Vec3 initialVector = targetPos.subtract(startPos);
       float radians = (float)angle * (float)Math.PI / 180.0F;
       double theta = Math.atan2(initialVector.z, initialVector.x);
       double horizontalDistanceSqr = initialVector.horizontalDistanceSqr();
       double horizontalDistance = Math.sqrt(horizontalDistanceSqr);
       double initialY = initialVector.y;
       double d4 = Math.sin(2.0F * radians);
       final double d5 = 0.08D;
       double cosSquared = Mth.square(Math.cos(radians));
       double sin = Math.sin(radians);
       double cos = Math.cos(radians);
       double zScale = Math.sin(theta);
       double xScale = Math.cos(theta);
       double jumpVelocitySqr = (horizontalDistanceSqr * d5) / (horizontalDistance * d4 - 2.0D * initialY * cosSquared);
       if (jumpVelocitySqr < 0.0D) {
          return null;
       } else {
          double jumpVelocity = Math.sqrt(jumpVelocitySqr);
          if (jumpVelocity > maxJumpVelocity) {
             return null;
          } else {
             double xzD = jumpVelocity * cos;
             double yD = jumpVelocity * sin;
             int numJumpSteps = Mth.ceil(horizontalDistance / xzD) * 2;
             double d15 = 0.0D;
             Vec3 prevJumpStep = null;

             for(int jumpStepIndex = 0; jumpStepIndex < numJumpSteps - 1; ++jumpStepIndex) {
                d15 += horizontalDistance / (double)numJumpSteps;
                double yShift = sin / cos * d15 - Mth.square(d15) * d5 / (2.0D * jumpVelocitySqr * Mth.square(cos));
                double xShift = d15 * xScale;
                double zShift = d15 * zScale;
                Vec3 nextJumpStep = startPos.add(xShift, yShift, zShift);
                if (prevJumpStep != null && !isClearTransition(mob, prevJumpStep, nextJumpStep)) {
                   return null;
                }

                prevJumpStep = nextJumpStep;
             }

             return (new Vec3(xzD * xScale, yD, xzD * zScale)).scale(0.95F);
          }
       }
    }

    public static boolean isClearTransition(Mob mob, Vec3 from, Vec3 to) {
       EntityDimensions longJumpingDimensions = mob.getDimensions(Pose.LONG_JUMPING);
       Vec3 diff = to.subtract(from);
       double d0 = Math.min(longJumpingDimensions.width, longJumpingDimensions.height);
       int numSteps = Mth.ceil(diff.length() / d0);
       Vec3 normDiff = diff.normalize();
       Vec3 step = from;

       for(int stepIndex = 0; stepIndex < numSteps; ++stepIndex) {
          step = stepIndex == numSteps - 1 ? to : step.add(normDiff.scale(d0 * (double)0.9F));
          AABB boundingBox = longJumpingDimensions.makeBoundingBox(step);
          if (!mob.level.noCollision(mob, boundingBox)) {
             return false;
          }
       }

       return true;
    }

   public static Optional<PositionTracker> getLongJumpTarget(LivingEntity mob) {
      return mob.getBrain().getMemory(COTWMemoryModuleTypes.LONG_JUMP_TARGET.get());
   }

    public static void setLongJumpTarget(LivingEntity mob, PositionTracker target) {
        mob.getBrain().setMemory(COTWMemoryModuleTypes.LONG_JUMP_TARGET.get(), target);
    }

    public static void clearLongJumpTarget(LivingEntity mob){
       mob.getBrain().eraseMemory(COTWMemoryModuleTypes.LONG_JUMP_TARGET.get());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isMidJump(LivingEntity mob) {
       return mob.getBrain().hasMemoryValue(MemoryModuleType.LONG_JUMP_MID_JUMP);
    }

    public static boolean isOnJumpCooldown(LivingEntity wolf){
        return wolf.getBrain().hasMemoryValue(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS);
    }
}
