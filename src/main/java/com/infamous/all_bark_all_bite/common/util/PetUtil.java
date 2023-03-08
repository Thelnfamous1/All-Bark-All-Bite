package com.infamous.all_bark_all_bite.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.Optional;
import java.util.UUID;

public class PetUtil {

    public static Optional<UUID> getOwnerUUID(Entity entity){
        if(entity instanceof OwnableEntity ownable) return Optional.ofNullable(ownable.getOwnerUUID());
        else if(entity instanceof AbstractHorse horse) return Optional.ofNullable(horse.getOwnerUUID());
        return Optional.empty();
    }

    public static Optional<Boolean> isTame(Entity entity){
        if(entity instanceof TamableAnimal tamableAnimal) return Optional.of(tamableAnimal.isTame());
        else if(entity instanceof AbstractHorse horse) return Optional.of(horse.isTamed());
        return Optional.empty();
    }

    public static boolean wantsToAttack(LivingEntity ignoredPet, LivingEntity target, LivingEntity owner){
        if (target instanceof OwnableEntity ownable) {
            return ownable.getOwner() != owner;
        }
        return true;
    }
}
