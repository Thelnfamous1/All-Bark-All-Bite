package com.infamous.all_bark_all_bite.mixin;

import com.infamous.all_bark_all_bite.common.util.PetManagement;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin extends Animal {

    protected TamableAnimalMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Inject(at = @At("RETURN"), method = "setOwnerUUID")
    private void handleSetOwnerUUID(UUID ownerUUID, CallbackInfo ci){
        if(ownerUUID != null && !this.level.isClientSide){
            PetManagement.getPetManager(this.level.dimension(), ownerUUID).add(this);
        }
    }
}
