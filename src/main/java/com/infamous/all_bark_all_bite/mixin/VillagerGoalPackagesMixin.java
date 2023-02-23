package com.infamous.all_bark_all_bite.mixin;

import com.google.common.collect.ImmutableList;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.util.BrainUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerGoalPackages.class)
public class VillagerGoalPackagesMixin {

    @Inject(at = @At("RETURN"), method = "getPlayPackage")
    private static void handleGetPlayPackage(float p_24584_, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>>> cir){
        boolean addedPlayLook = false; // Look behavior is added before interact behavior for PLAY
        boolean addedPlayInteract = false;
        for(Pair<Integer, ? extends Behavior<? super Villager>> prioritizedBehavior : cir.getReturnValue()){
            if(addedPlayLook && addedPlayInteract) break;
            if(prioritizedBehavior.getFirst() == 5 && prioritizedBehavior.getSecond() instanceof RunOne<?> runOne){
                if(!addedPlayLook){
                    // covered by handleGetFullLookBehavior
                    addedPlayLook = true;
                    continue;
                }
                BrainUtil.getGateBehaviors(runOne).add(InteractWith.of(ABABEntityTypes.DOG.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1);
                addedPlayInteract = true;
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getIdlePackage")
    private static void handleGetIdlePackage(VillagerProfession p_24599_, float p_24600_, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>>> cir){
        // Interact behavior is added before look behavior for IDLE
        for(Pair<Integer, ? extends Behavior<? super Villager>> prioritizedBehavior : cir.getReturnValue()){
            if(prioritizedBehavior.getFirst() == 2 && prioritizedBehavior.getSecond() instanceof RunOne<?> runOne){
                BrainUtil.getGateBehaviors(runOne).add(InteractWith.of(ABABEntityTypes.DOG.get(), 8, MemoryModuleType.INTERACTION_TARGET, 0.5F, 2), 1);
                break;
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getFullLookBehavior")
    private static void handleGetFullLookBehavior(CallbackInfoReturnable<Pair<Integer, Behavior<LivingEntity>>> cir){
        if(cir.getReturnValue().getSecond() instanceof RunOne<?> runOne){
            BrainUtil.getGateBehaviors(runOne).add(new SetEntityLookTarget(ABABEntityTypes.DOG.get(), 8.0F), 8);
        }
    }
}
