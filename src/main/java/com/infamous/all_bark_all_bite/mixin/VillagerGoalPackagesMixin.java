package com.infamous.all_bark_all_bite.mixin;

import com.google.common.collect.ImmutableList;
import com.infamous.all_bark_all_bite.common.entity.dog.DogHooks;
import com.infamous.all_bark_all_bite.common.registry.ABABEntityTypes;
import com.infamous.all_bark_all_bite.common.util.ai.BrainUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
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
        DogHooks.addVillagerDogPlayBehaviors(cir.getReturnValue());
    }

    @Inject(at = @At("RETURN"), method = "getIdlePackage")
    private static void handleGetIdlePackage(VillagerProfession p_24599_, float p_24600_, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>>> cir){
        DogHooks.addVillagerDogIdleBehaviors(cir.getReturnValue());
    }

    @Inject(at = @At("RETURN"), method = "getFullLookBehavior")
    private static void handleGetFullLookBehavior(CallbackInfoReturnable<Pair<Integer, BehaviorControl<LivingEntity>>> cir){
        if(cir.getReturnValue().getSecond() instanceof RunOne<?> runOne){
            BrainUtil.getGateBehaviors(runOne).add(SetEntityLookTarget.create(ABABEntityTypes.DOG.get(), 8.0F), 8);
        }
    }
}
