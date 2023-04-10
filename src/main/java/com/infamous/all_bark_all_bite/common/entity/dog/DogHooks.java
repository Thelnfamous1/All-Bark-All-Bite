package com.infamous.all_bark_all_bite.common.entity.dog;

import com.google.common.collect.ImmutableList;
import com.infamous.all_bark_all_bite.common.entity.SharedWolfAi;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.util.ai.BrainUtil;
import com.infamous.all_bark_all_bite.common.util.EntityDimensionsUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;

public class DogHooks {
    public static EntityDimensions onDogSize(Entity entity, EntityDimensions originalSize) {
        originalSize = EntityDimensionsUtil.resetIfSleeping(entity, originalSize);
        originalSize = EntityDimensionsUtil.unfixIfNeeded(originalSize);
        originalSize = EntityDimensionsUtil.resizeForLongJumpIfNeeded(entity, originalSize, SharedWolfAi.LONG_JUMPING_SCALE);
        return originalSize;
    }

    public static void addVillagerDogPlayBehaviors(ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> behaviors) {
        // Look behavior is added before interact behavior for PLAY
        boolean addedPlayLook = false;
        boolean addedPlayInteract = false;
        for(Pair<Integer, ? extends BehaviorControl<? super Villager>> prioritizedBehavior : behaviors){
            if(addedPlayLook && addedPlayInteract) break;
            if(prioritizedBehavior.getFirst() == 5 && prioritizedBehavior.getSecond() instanceof RunOne<?> runOne){
                if(!addedPlayLook){
                    // covered by VillagerGoalPackagesMixin.handleGetFullLookBehavior
                    addedPlayLook = true;
                    continue;
                }
                BrainUtil.getGateBehaviors(runOne).add(InteractWith.of(ABABEntityTypes.DOG.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1);
                addedPlayInteract = true;
            }
        }
    }

    public static void addVillagerDogIdleBehaviors(ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> behaviors) {
        // Interact behavior is added before look behavior for IDLE
        for(Pair<Integer, ? extends BehaviorControl<? super Villager>> prioritizedBehavior : behaviors){
            if(prioritizedBehavior.getFirst() == 2 && prioritizedBehavior.getSecond() instanceof RunOne<?> runOne){
                BrainUtil.getGateBehaviors(runOne).add(InteractWith.of(ABABEntityTypes.DOG.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1);
                break;
            }
        }
    }
}
